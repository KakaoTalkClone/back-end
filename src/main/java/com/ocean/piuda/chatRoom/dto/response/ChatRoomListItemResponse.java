package com.ocean.piuda.chatRoom.dto.response;

import com.ocean.piuda.chatRoom.enums.ChatRoomType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ChatRoomListItemResponse(
        Long roomId,
        String roomName,        // 1:1인 경우 상대방 이름, 그룹인 경우 방 이름
        ChatRoomType roomType,
        String lastMessagePreview,
        LocalDateTime lastMessageAt,
        long unreadCount,
        String thumbnailUrl     // 1:1인 경우 상대방 프사, 그룹인 경우 마지막 발화자 프사
) {
}