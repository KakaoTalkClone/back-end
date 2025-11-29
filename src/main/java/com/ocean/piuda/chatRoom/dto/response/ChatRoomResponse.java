package com.ocean.piuda.chatRoom.dto.response;

import com.ocean.piuda.chatRoom.entity.ChatRoom;
import com.ocean.piuda.chatRoom.enums.ChatRoomType;
import lombok.Builder;

@Builder
public record ChatRoomResponse(
        Long roomId,
        String roomName,
        ChatRoomType chatRoomType
) {
    public static ChatRoomResponse from(ChatRoom chatRoom) {
        return ChatRoomResponse.builder()
                .roomId(chatRoom.getRoomId())
                .roomName(chatRoom.getRoomName())
                .chatRoomType(chatRoom.getChatRoomType())
                .build();
    }
}
