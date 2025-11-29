package com.ocean.piuda.chatRoom.service;

import com.ocean.piuda.chat.repository.MessageRepository;
import com.ocean.piuda.chatRoom.dto.response.ChatRoomListItemResponse;
import com.ocean.piuda.chatRoom.entity.ChatRoom;
import com.ocean.piuda.chatRoom.repository.RoomRepository;
import com.ocean.piuda.global.api.exception.BusinessException;
import com.ocean.piuda.global.api.exception.ExceptionType;
import com.ocean.piuda.user.repository.RoomUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomQueryService {

    private final RoomRepository roomRepository;
    private final RoomUserRepository roomUserRepository;
    private final MessageRepository messageRepository;

    public ChatRoom getRoomById(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));
    }

    /**
     * 내가 참여 중인 채팅방 목록 + 마지막 메시지 + 안 읽은 메시지 개수
     * (N+1 문제 해결된 버전)
     */
    public Page<ChatRoomListItemResponse> getMyRooms(Long userId, Pageable pageable) {

        // 개선된 로직: 한 방 쿼리로 DTO까지 매핑해서 가져옴
        return roomUserRepository.findMyRoomListDto(userId, pageable);
    }
}