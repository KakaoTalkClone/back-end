package com.ocean.piuda.websocket.handler;

import com.ocean.piuda.security.jwt.service.CustomUserDetailsService;
import com.ocean.piuda.security.jwt.util.JwtTokenProvider;
import com.ocean.piuda.security.oauth2.principal.PrincipalDetails;
import com.ocean.piuda.user.repository.RoomUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@RequiredArgsConstructor
@Component
public class StompJwtChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final RoomUserRepository roomUserRepository; // 구독 권한 확인용 리포지토리

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        // CONNECT, SEND, SUBSCRIBE 등 인증이 필요한 명령어 처리
        if (StompCommand.CONNECT.equals(accessor.getCommand()) ||
                StompCommand.SEND.equals(accessor.getCommand()) ||
                StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {

            String authHeader = accessor.getFirstNativeHeader("Authorization");
            String token = resolveToken(authHeader);

            if (token != null && jwtTokenProvider.isValidToken(token)) {
                Long userId = jwtTokenProvider.extractId(token);

                // 1. 유저 인증 정보 로드 및 SecurityContext 설정
                PrincipalDetails principal = (PrincipalDetails) customUserDetailsService.loadUserById(userId);
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        principal.getAuthorities()
                );
                accessor.setUser(authentication);

                // 2. SUBSCRIBE 일 때, 방 멤버십 확인 (구독 권한 체크)
                if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                    validateSubscription(accessor.getDestination(), userId);
                }

                log.debug("[STOMP] 인증/인가 성공: command={}, userId={}", accessor.getCommand(), userId);

            } else {
                if(StompCommand.CONNECT.equals(accessor.getCommand())) {
                    log.warn("[STOMP] CONNECT 실패: 유효하지 않은 토큰. header={}", authHeader);
                }
                // 토큰이 유효하지 않으면 메시지 처리를 중단하거나 예외를 던질 수 있음
                // 여기서는 CONNECT 시에만 로그를 남기고, 실제로는 Spring Security 설정에 따라 처리됨
                // (엄격하게 하려면 여기서 throw new AccessDeniedException("Invalid Token"); 해도 됨)
            }
        }

        return message;
    }

    /**
     * [보안] 구독 권한 검증
     * - /topic/chatroom.{roomId} 패턴인 경우, 해당 유저가 그 방의 멤버인지 DB 조회
     */
    private void validateSubscription(String destination, Long userId) {
        if (destination == null) return;

        // 채팅방 구독 패턴인지 확인 (/topic/chatroom.1)
        if (destination.startsWith("/topic/chatroom.")) {
            try {
                String roomIdStr = destination.substring("/topic/chatroom.".length());
                Long roomId = Long.parseLong(roomIdStr);

                // DB 조회: 해당 방에 유저가 존재하는지 확인
                boolean isMember = roomUserRepository.findByChatRoomRoomIdAndUserId(roomId, userId).isPresent();

                if (!isMember) {
                    log.warn("[STOMP] 구독 거부: User {}는 Room {}의 멤버가 아닙니다.", userId, roomId);
                    throw new AccessDeniedException("해당 채팅방의 구독 권한이 없습니다.");
                }
            } catch (NumberFormatException e) {
                log.error("[STOMP] 잘못된 구독 경로 형식: {}", destination);
                // 형식이 잘못되었어도 보안상 구독을 막는 것이 안전함
                throw new AccessDeniedException("잘못된 구독 경로입니다.");
            }
        }
    }

    private String resolveToken(String authHeader) {
        if (!StringUtils.hasText(authHeader)) {
            return null;
        }
        if (authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return authHeader;
    }
}