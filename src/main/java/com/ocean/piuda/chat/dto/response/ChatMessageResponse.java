package com.ocean.piuda.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat; // import 추가
import com.ocean.piuda.chat.entity.Message;
import com.ocean.piuda.chat.enums.ContentType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ChatMessageResponse(
        Long roomId,
        Long messageId,
        Long senderId,
        String senderNickname,
        String content,
        ContentType contentType,

        // 날짜 포맷 강제 지정 (이게 없으면 웹소켓 전송 시 에러 날 수 있음)
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,

        int unreadCount
) {
    public static ChatMessageResponse from(Message m, int unreadCount) {
        return ChatMessageResponse.builder()
                .roomId(m.getChatRoom().getRoomId())
                .messageId(m.getMessageId())
                .senderId(m.getSender().getId())
                .senderNickname(m.getSender().getNickname())
                .content(m.getContent())
                .contentType(m.getContentType())
                .createdAt(m.getCreatedAt())
                .unreadCount(unreadCount)
                .build();
    }
}