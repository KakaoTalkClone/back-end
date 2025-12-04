package com.ocean.piuda.chat.controller;

import com.ocean.piuda.chat.dto.response.ChatMessageResponse;
import com.ocean.piuda.chat.dto.request.ChatMessageSendRequest;
import com.ocean.piuda.chat.dto.request.ChatReadRequest;
import com.ocean.piuda.chat.dto.response.ChatReadResponse;
import com.ocean.piuda.chat.service.ChatService;
import com.ocean.piuda.security.oauth2.principal.PrincipalDetails;
import com.ocean.piuda.user.entity.RoomUser;
import com.ocean.piuda.user.repository.RoomUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    // [추가됨] 채팅방 목록 갱신 대상을 찾기 위해 필요
    private final RoomUserRepository roomUserRepository;

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

            // 1. 메시지 저장 및 처리
            ChatMessageResponse response = chatService.sendMessage(senderId, req);
            log.info(">>> [STOMP] 3. 메시지 저장 완료: ID {}", response.messageId());

            // 2. [채팅방 내부] 구독자들에게 메시지 전송
            String roomDestination = "/topic/chatroom." + response.roomId();
            messagingTemplate.convertAndSend(roomDestination, response);
            log.info(">>> [STOMP] 4. 채팅방 발송 완료: {}", roomDestination);

            // 3. [채팅방 목록] 갱신을 위해 해당 방의 모든 멤버에게 알림 전송 (Real-time List Update)
            List<RoomUser> members = roomUserRepository.findByChatRoomRoomId(req.roomId());
            for (RoomUser member : members) {
                // 각 유저의 개인 채널: /topic/user.{userId}
                String userDestination = "/topic/user." + member.getUser().getId();
                messagingTemplate.convertAndSend(userDestination, response);
            }
            log.info(">>> [STOMP] 5. 목록 갱신 알림 완료 (대상 {}명)", members.size());

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
            if (response != null) {
                messagingTemplate.convertAndSend("/topic/chatroom." + req.roomId(), response);
                log.info(">>> [STOMP] 읽음 처리 방송: User {} -> Msg {}", userId, req.messageId());

                // (선택사항) 읽음 처리에 따라 채팅방 목록의 뱃지도 줄어들어야 한다면
                // 여기서도 /topic/user.{userId} 로 쏘아줄 수 있음.
                // 하지만 보통 '내가 읽은 것'은 클라이언트가 바로 알고 있으므로 생략 가능.
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