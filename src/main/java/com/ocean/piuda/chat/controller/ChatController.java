package com.ocean.piuda.chat.controller;

import com.ocean.piuda.chat.dto.response.ChatMessageResponse;
import com.ocean.piuda.chat.dto.request.ChatMessageSendRequest;
import com.ocean.piuda.chat.dto.request.ChatReadRequest;
import com.ocean.piuda.chat.dto.response.ChatReadResponse;
import com.ocean.piuda.chat.service.ChatService;
import com.ocean.piuda.security.oauth2.principal.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@RequiredArgsConstructor
@Controller
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 메시지 전송
     * Destination: /pub/chat/message
     */
    @MessageMapping("/chat/message")
    public void handleChatMessage(
            ChatMessageSendRequest req,
            Principal principal
    ) {
        log.info(">>> [STOMP] 1. 요청 수신: {}", req);

        try {
            Long senderId = getUserIdFromPrincipal(principal);
            if (senderId == null) return;

            log.info(">>> [STOMP] 2. 인증된 Sender ID: {}", senderId);

            ChatMessageResponse response = chatService.sendMessage(senderId, req);
            log.info(">>> [STOMP] 3. 메시지 저장 완료: ID {}", response.messageId());

            String destination = "/topic/chatroom." + response.roomId();
            messagingTemplate.convertAndSend(destination, response);
            log.info(">>> [STOMP] 4. 발송 완료: {}", destination);

        } catch (Exception e) {
            log.error(">>> [STOMP] 처리 중 예상치 못한 에러 발생", e);
        }
    }

    /**
     * 읽음 처리 요청
     * Destination: /pub/chat/read
     */
    @MessageMapping("/chat/read")
    public void handleRead(
            ChatReadRequest req,
            Principal principal
    ) {
        try {
            Long userId = getUserIdFromPrincipal(principal);
            if (userId == null) return;

            // 1. 서비스 호출
            ChatReadResponse response = chatService.markStreamAsRead(userId, req);

            // 2. [핵심] 변경사항이 있을 때만(null이 아닐 때만) 브로드캐스트
            // 이미 읽은 메시지에 대해 또 요청이 오면 response는 null임
            if (response != null) {
                messagingTemplate.convertAndSend("/topic/chatroom." + req.roomId(), response);
                log.info(">>> [STOMP] 읽음 처리 방송: User {} -> Msg {}", userId, req.messageId());
            }

        } catch (Exception e) {
            log.error(">>> [STOMP] 읽음 처리 중 에러", e);
        }
    }

    /**
     * Principal에서 UserID 안전하게 추출
     */
    private Long getUserIdFromPrincipal(Principal principal) {
        if (principal == null) {
            log.error(">>> [STOMP] Principal is NULL.");
            return null;
        }

        if (principal instanceof UsernamePasswordAuthenticationToken token) {
            PrincipalDetails details = (PrincipalDetails) token.getPrincipal();
            if (details.getUser() != null) {
                return details.getUser().getId();
            }
        }

        try {
            return Long.parseLong(principal.getName());
        } catch (NumberFormatException e) {
            log.error(">>> [STOMP] ID 추출 실패. Name({})도 숫자가 아님", principal.getName());
            return null;
        }
    }
}