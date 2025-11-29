package com.ocean.piuda.chat.service;

import com.ocean.piuda.chat.dto.request.ChatMessageSendRequest;
import com.ocean.piuda.chat.dto.request.ChatReadRequest;
import com.ocean.piuda.chat.dto.response.ChatMessageResponse;
import com.ocean.piuda.chat.dto.response.ChatReadResponse;
import com.ocean.piuda.chatRoom.dto.response.ChatRoomResponse;
import com.ocean.piuda.chat.entity.DirectChat;
import com.ocean.piuda.chat.entity.Message;
import com.ocean.piuda.chat.enums.ContentType;
import com.ocean.piuda.chat.repository.DirectChatRepository;
import com.ocean.piuda.chat.repository.MessageRepository;
import com.ocean.piuda.chatRoom.repository.RoomRepository;
import com.ocean.piuda.chatRoom.entity.ChatRoom;
import com.ocean.piuda.chatRoom.enums.ChatRoomType;
import com.ocean.piuda.global.api.exception.BusinessException;
import com.ocean.piuda.global.api.exception.ExceptionType;
import com.ocean.piuda.user.entity.RoomUser;
import com.ocean.piuda.user.entity.User;
import com.ocean.piuda.user.repository.RoomUserRepository;
import com.ocean.piuda.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ChatService {

    private final RoomRepository roomRepository;
    private final RoomUserRepository roomUserRepository;
    private final DirectChatRepository directChatRepository;
    private final MessageRepository messageRepository;
    private final UserQueryService userQueryService;
    private final com.ocean.piuda.chatRoom.service.ChatRoomQueryService chatRoomQueryService;

    /**
     * 채팅 메시지 전송
     */
    @Transactional
    public ChatMessageResponse sendMessage(Long senderId, ChatMessageSendRequest req) {
        ChatRoom chatRoom = chatRoomQueryService.getRoomById(req.roomId());
        User sender = userQueryService.getUserById(senderId);

        // 방 참여자 검증 [수정됨]
        roomUserRepository.findByChatRoomRoomIdAndUserId(chatRoom.getRoomId(), senderId)
                .orElseThrow(() -> new BusinessException(ExceptionType.ACCESS_DENIED));

        ContentType contentType = req.contentType() != null ? req.contentType() : ContentType.TEXT;

        Message message = Message.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .content(req.content())
                .contentType(contentType)
                .build();

        Message savedMessage = messageRepository.save(message);

        // 마지막 메시지 갱신
        chatRoom.updateLastMessage(savedMessage);

        // 보낸 사람의 lastReadMessage 갱신 (자기가 보낸건 자기가 읽은 것이므로) [수정됨]
        roomUserRepository.findByChatRoomRoomIdAndUserId(chatRoom.getRoomId(), senderId)
                .ifPresent(ru -> ru.markAsRead(savedMessage));

        // 안 읽은 사람 수 계산
        int unreadCount = (int) roomUserRepository.countUnreadMembersForMessage(
                chatRoom.getRoomId(),
                senderId,
                savedMessage.getMessageId()
        );

        return ChatMessageResponse.from(savedMessage, unreadCount);
    }

    /**
     * 방 메시지 조회 + 읽음 처리 (입장 시 호출)
     */
    @Transactional
    public Page<ChatMessageResponse> getMessages(Long roomId, Long userId, int page, int size) {
        // [수정됨]
        RoomUser roomUser = roomUserRepository.findByChatRoomRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new BusinessException(ExceptionType.ACCESS_DENIED));

        // [수정됨]
        Page<Message> messagePage =
                messageRepository.findByChatRoomRoomIdOrderByMessageIdDesc(roomId, PageRequest.of(page, size));

        // 조회한 메시지 중 가장 최신 메시지까지 '읽음' 처리
        Message latest = messagePage.getContent().stream().findFirst().orElse(null);
        if (latest != null &&
                (roomUser.getLastReadMessage() == null
                        || roomUser.getLastReadMessage().getMessageId() < latest.getMessageId())) {
            roomUser.markAsRead(latest);
        }

        return messagePage.map(m -> {
            int unreadCount = (int) roomUserRepository.countUnreadMembersForMessage(
                    roomId,
                    m.getSender().getId(),
                    m.getMessageId()
            );
            return ChatMessageResponse.from(m, unreadCount);
        });
    }

    /**
     * [핵심] 메시지 읽음 처리 (실시간 소켓 요청용)
     * 이미 읽은 위치보다 과거의 요청이 오면 무시(null 반환)하여 중복 브로드캐스트 방지
     */
    @Transactional
    public ChatReadResponse markStreamAsRead(Long userId, ChatReadRequest req) {
        // [수정됨]
        RoomUser roomUser = roomUserRepository.findByChatRoomRoomIdAndUserId(req.roomId(), userId)
                .orElseThrow(() -> new BusinessException(ExceptionType.ACCESS_DENIED));

        Message message = messageRepository.findById(req.messageId())
                .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));

        // 1. 중복 체크: 이미 읽은 메시지 ID보다 작거나 같으면 업데이트 안 함
        if (roomUser.getLastReadMessage() != null &&
                roomUser.getLastReadMessage().getMessageId() >= message.getMessageId()) {
            return null; // 변경 없음
        }

        // 2. 업데이트 수행
        roomUser.markAsRead(message);

        return ChatReadResponse.builder()
                .roomId(req.roomId())
                .userId(userId)
                .messageId(req.messageId())
                .build();
    }

    /**
     * 1:1 채팅방 보장
     */
    @Transactional
    public ChatRoomResponse ensureDirectRoom(Long meId, Long targetUserId) {
        User me = userQueryService.getUserById(meId);
        User target = userQueryService.getUserById(targetUserId);

        User min = me.getId() < target.getId() ? me : target;
        User max = me.getId() < target.getId() ? target : me;

        return directChatRepository.findByUserAIdAndUserBId(min.getId(), max.getId())
                .map(direct -> ChatRoomResponse.from(direct.getChatRoom()))
                .orElseGet(() -> createDirectRoomWithConcurrencyHandling(min, max, me, target));
    }

    @Transactional(noRollbackFor = DataIntegrityViolationException.class)
    protected ChatRoomResponse createDirectRoomWithConcurrencyHandling(User min, User max, User me, User target) {
        try {
            ChatRoom chatRoom = createDirectRoomEntity();
            chatRoom = roomRepository.save(chatRoom);

            RoomUser meRu = RoomUser.builder().chatRoom(chatRoom).user(me).build();
            RoomUser targetRu = RoomUser.builder().chatRoom(chatRoom).user(target).build();

            roomUserRepository.save(meRu);
            roomUserRepository.save(targetRu);

            DirectChat directChat = directChatRepository.save(DirectChat.of(min, max, chatRoom));
            return ChatRoomResponse.from(directChat.getChatRoom());
        } catch (DataIntegrityViolationException e) {
            DirectChat existing = directChatRepository.findByUserAIdAndUserBId(min.getId(), max.getId())
                    .orElseThrow(() -> new BusinessException(ExceptionType.UNEXPECTED_SERVER_ERROR));
            return ChatRoomResponse.from(existing.getChatRoom());
        }
    }

    /**
     * 그룹 채팅방 생성
     */
    @Transactional
    public ChatRoomResponse createGroupRoom(String roomName, List<Long> participantUserIds) {
        if (participantUserIds == null || participantUserIds.isEmpty()) {
            throw new BusinessException(ExceptionType.ESSENTIAL_FIELD_MISSING_ERROR);
        }

        List<User> participants = participantUserIds.stream()
                .distinct()
                .map(userQueryService::getUserById)
                .toList();

        ChatRoom chatRoom = ChatRoom.builder()
                .roomName(roomName)
                .chatRoomType(ChatRoomType.GROUP)
                .build();

        chatRoom = roomRepository.save(chatRoom);

        for (User u : participants) {
            RoomUser ru = RoomUser.builder()
                    .chatRoom(chatRoom)
                    .user(u)
                    .build();
            roomUserRepository.save(ru);
        }

        return ChatRoomResponse.from(chatRoom);
    }

    /**
     * 그룹방 초대
     */
    @Transactional
    public void inviteUsersToRoom(Long roomId, Long inviterId, List<Long> targetUserIds) {
        ChatRoom chatRoom = chatRoomQueryService.getRoomById(roomId);

        if (chatRoom.getChatRoomType() == ChatRoomType.DIRECT) {
            throw new BusinessException(ExceptionType.INVALID_VALUE_ERROR, Map.of("message", "1:1 채팅방에는 초대할 수 없습니다."));
        }

        // [수정됨]
        roomUserRepository.findByChatRoomRoomIdAndUserId(roomId, inviterId)
                .orElseThrow(() -> new BusinessException(ExceptionType.ACCESS_DENIED));

        List<User> targets = targetUserIds.stream()
                .distinct()
                .map(userQueryService::getUserById)
                .toList();

        for (User target : targets) {
            // [수정됨]
            if (roomUserRepository.findByChatRoomRoomIdAndUserId(roomId, target.getId()).isPresent()) continue;

            RoomUser ru = RoomUser.builder()
                    .chatRoom(chatRoom)
                    .user(target)
                    .build();
            roomUserRepository.save(ru);
        }
    }

    /**
     * 방 나가기
     */
    @Transactional
    public void leaveRoom(Long roomId, Long userId) {
        // [수정됨]
        RoomUser roomUser = roomUserRepository.findByChatRoomRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));

        roomUserRepository.delete(roomUser);
    }

    protected ChatRoom createDirectRoomEntity() {
        return ChatRoom.builder()
                .chatRoomType(ChatRoomType.DIRECT)
                .build();
    }
}