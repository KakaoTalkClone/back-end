package com.ocean.piuda.chatRoom.dto.response;

import com.ocean.piuda.chatRoom.enums.ChatRoomType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ChatRoomListItemResponse(
        Long roomId,
        String roomName,
        ChatRoomType roomType,
        String lastMessagePreview,
        LocalDateTime lastMessageAt,
        long unreadCount      // 이 방에서 내가 안 읽은 메시지 개수
) {
}
