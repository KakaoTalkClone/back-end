package com.ocean.piuda.notification.service;

import com.google.firebase.messaging.*;
import com.ocean.piuda.notification.dto.request.SaveTokenRequest;
import com.ocean.piuda.notification.dto.request.SendNotificationRequest;
import com.ocean.piuda.notification.dto.response.SendResultResponse;
import com.ocean.piuda.notification.entity.FcmToken;
import com.ocean.piuda.notification.repository.FcmTokenRepository;
import com.ocean.piuda.user.entity.User;
import com.ocean.piuda.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FcmCommandService {

    private static final int FCM_MULTICAST_LIMIT = 500;

    private final FirebaseMessaging messaging;
    private final FcmTokenRepository fcmTokenRepository;
    private final UserQueryService userQueryService;

    public FcmToken upsert(Long userId, SaveTokenRequest req) {
        User user = userQueryService.getUserById(userId);
        FcmToken existing = fcmTokenRepository.findByToken(req.token()).orElse(null);
        if (existing != null) {
            existing.upsertUser(user);
            existing.updateClientInfo(req.deviceId(), req.platform());
            return existing;
        }
        return fcmTokenRepository.save(
                FcmToken.builder()
                        .user(user)
                        .token(req.token())
                        .deviceId(req.deviceId())
                        .platform(req.platform())
                        .build()
        );
    }

    public void removeByToken(String token) {
        fcmTokenRepository.deleteByToken(token);
    }

    public SendResultResponse send(SendNotificationRequest req) throws FirebaseMessagingException {
        Set<String> targetTokens = new LinkedHashSet<>();

        if (req.userIds() != null && !req.userIds().isEmpty()) {
            List<User> users = req.userIds().stream()
                    .map(userQueryService::getUserById)
                    .toList();
            users.forEach(u -> fcmTokenRepository.findAllByUser(u).forEach(t -> targetTokens.add(t.getToken())));
        }

        if (req.tokens() != null && !req.tokens().isEmpty()) targetTokens.addAll(req.tokens());

        if (targetTokens.isEmpty()) {
            return SendResultResponse.builder()
                    .successCount(0).failureCount(0).messageIds(List.of())
                    .build();
        }

        int success = 0, failure = 0;
        List<String> messageIds = new ArrayList<>();

        for (List<String> chunk : partition(new ArrayList<>(targetTokens), FCM_MULTICAST_LIMIT)) {
            SendResultResponse res = sendToTokens(chunk, req.title(), req.body(), req.url());
            success += res.successCount();
            failure += res.failureCount();
            if (res.messageIds() != null) messageIds.addAll(res.messageIds());
        }

        return SendResultResponse.builder()
                .successCount(success)
                .failureCount(failure)
                .messageIds(messageIds)
                .build();
    }

    /**
     * [수정됨] 실시간 채팅 알림 전송
     * - roomId를 '공통 데이터(putData)'에 포함시켜 웹에서도 받을 수 있게 수정함
     */
    public void sendChatPush(Long senderId, String senderNickname, String content, Long roomId, List<Long> targetUserIds) {
        if (targetUserIds == null || targetUserIds.isEmpty()) return;

        List<Long> actualTargets = targetUserIds.stream()
                .filter(id -> !id.equals(senderId))
                .toList();

        if (actualTargets.isEmpty()) return;

        Set<String> tokens = new HashSet<>();
        for (Long targetId : actualTargets) {
            try {
                User user = userQueryService.getUserById(targetId);
                fcmTokenRepository.findAllByUser(user).forEach(t -> tokens.add(t.getToken()));
            } catch (Exception e) {
                log.warn("채팅 알림 대상 유저 조회 실패: {}", targetId);
            }
        }

        if (tokens.isEmpty()) return;

        for (List<String> chunk : partition(new ArrayList<>(tokens), FCM_MULTICAST_LIMIT)) {
            try {
                MulticastMessage msg = MulticastMessage.builder()
                        // ★★★ [핵심 수정] 공통 데이터 영역에 roomId 추가 (웹/안드/iOS 모두 수신 가능) ★★★
                        .putData("type", "CHAT")
                        .putData("roomId", String.valueOf(roomId))
                        .putData("senderName", senderNickname)

                        // [Web Push 설정]
                        .setWebpushConfig(WebpushConfig.builder()
                                .setNotification(WebpushNotification.builder()
                                        .setTitle(senderNickname)
                                        .setBody(content)
                                        .build())
                                // withLink를 제거하거나 유지해도 됨 (JS에서 처리하므로)
                                .build())

                        // [Android 설정] - 공통 데이터가 있으므로 putData 중복 생략 가능하지만 명시적 유지
                        .setAndroidConfig(AndroidConfig.builder()
                                .setPriority(AndroidConfig.Priority.HIGH)
                                .setNotification(AndroidNotification.builder()
                                        .setTitle(senderNickname)
                                        .setBody(content)
                                        .setClickAction("FLUTTER_NOTIFICATION_CLICK")
                                        .build())
                                .build())

                        // [iOS(APNs) 설정]
                        .setApnsConfig(ApnsConfig.builder()
                                .setAps(Aps.builder()
                                        .setAlert(ApsAlert.builder()
                                                .setTitle(senderNickname)
                                                .setBody(content)
                                                .build())
                                        .setSound("default")
                                        // iOS는 customData가 별도라 여기도 필요함
                                        .putCustomData("type", "CHAT")
                                        .putCustomData("roomId", String.valueOf(roomId))
                                        .build())
                                .build())
                        .addAllTokens(chunk)
                        .build();

                messaging.sendEachForMulticast(msg);
                log.info("채팅 알림 발송 완료: 방 #{} -> {}개 토큰", roomId, chunk.size());

            } catch (FirebaseMessagingException e) {
                log.error("채팅 알림 발송 실패", e);
            }
        }
    }

    public SendResultResponse sendToUser(Long userId, String title, String body, String url) throws FirebaseMessagingException {
        User user = userQueryService.getUserById(userId);
        List<String> tokens = fcmTokenRepository.findAllByUser(user).stream().map(FcmToken::getToken).toList();
        return sendToTokens(tokens, title, body, url);
    }

    public SendResultResponse sendToTokens(List<String> tokens, String title, String body, String url) throws FirebaseMessagingException {
        if (tokens == null || tokens.isEmpty()) {
            return SendResultResponse.builder().successCount(0).failureCount(0).messageIds(List.of()).build();
        }

        MulticastMessage msg = MulticastMessage.builder()
                .putData("url", url == null ? "/" : url)
                .setWebpushConfig(WebpushConfig.builder()
                        .setNotification(WebpushNotification.builder()
                                .setTitle(Objects.requireNonNullElse(title, "알림"))
                                .setBody(Objects.requireNonNullElse(body, ""))
                                .build())
                        .setFcmOptions(WebpushFcmOptions.withLink(url == null ? "/" : url))
                        .putHeader("TTL", String.valueOf(Duration.ofMinutes(5).toSeconds()))
                        .build())
                .addAllTokens(tokens)
                .build();

        BatchResponse batch = messaging.sendEachForMulticast(msg);

        List<String> toDelete = new ArrayList<>();
        List<String> messageIds = new ArrayList<>();

        for (int i = 0; i < batch.getResponses().size(); i++) {
            SendResponse r = batch.getResponses().get(i);
            if (r.isSuccessful()) {
                if (r.getMessageId() != null) {
                    messageIds.add(r.getMessageId());
                }
            } else if (r.getException() instanceof FirebaseMessagingException fme) {
                MessagingErrorCode code = fme.getMessagingErrorCode();
                if (code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.INVALID_ARGUMENT) {
                    toDelete.add(tokens.get(i));
                }
            }
        }
        toDelete.forEach(fcmTokenRepository::deleteByToken);

        return SendResultResponse.builder()
                .successCount(batch.getSuccessCount())
                .failureCount(batch.getFailureCount())
                .messageIds(messageIds)
                .build();
    }

    private static <T> List<List<T>> partition(List<T> list, int size) {
        if (size <= 0) throw new IllegalArgumentException("size must be > 0");
        int n = list.size();
        List<List<T>> parts = new ArrayList<>((n + size - 1) / size);
        for (int i = 0; i < n; i += size) {
            parts.add(list.subList(i, Math.min(n, i + size)));
        }
        return parts;
    }
}