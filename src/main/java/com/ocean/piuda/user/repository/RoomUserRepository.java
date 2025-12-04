package com.ocean.piuda.user.repository;

import com.ocean.piuda.chatRoom.dto.response.ChatRoomListItemResponse;
import com.ocean.piuda.user.entity.RoomUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomUserRepository extends JpaRepository<RoomUser, Long> {

    List<RoomUser> findByUserId(Long userId);

    Page<RoomUser> findByUserId(Long userId, Pageable pageable);

    Optional<RoomUser> findByChatRoomRoomIdAndUserId(Long roomId, Long userId);

    // 해당 방의 모든 참여자 조회 (채팅방 목록 갱신 알림용)
    List<RoomUser> findByChatRoomRoomId(Long roomId);

    @Query("""
        select count(ru)
        from RoomUser ru
        where ru.chatRoom.roomId = :roomId
          and ru.user.id <> :senderId
          and (ru.lastReadMessage is null or ru.lastReadMessage.messageId < :messageId)
    """)
    long countUnreadMembersForMessage(
            @Param("roomId") Long roomId,
            @Param("senderId") Long senderId,
            @Param("messageId") Long messageId
    );

    @Query("""
        SELECT new com.ocean.piuda.chatRoom.dto.response.ChatRoomListItemResponse(
            r.roomId,
            CASE 
                WHEN r.chatRoomType = com.ocean.piuda.chatRoom.enums.ChatRoomType.DIRECT 
                THEN COALESCE(otherU.nickname, '알 수 없는 사용자')
                ELSE r.roomName 
            END,
            r.chatRoomType,
            lm.content,
            lm.createdAt,
            (
                SELECT count(m)
                FROM Message m
                WHERE m.chatRoom.roomId = r.roomId
                  AND m.messageId > COALESCE(ru.lastReadMessage.messageId, 0)
            ),
            (
                SELECT i.url
                FROM UserImage ui
                JOIN ui.image i
                WHERE ui.type = com.ocean.piuda.image.enums.UserImageType.PROFILE
                  AND ui.user.id = (
                      CASE 
                          WHEN r.chatRoomType = com.ocean.piuda.chatRoom.enums.ChatRoomType.DIRECT 
                          THEN otherU.id 
                          ELSE lm.sender.id 
                      END
                  )
            )
        )
        FROM RoomUser ru
        JOIN ru.chatRoom r
        LEFT JOIN r.lastMessage lm
        LEFT JOIN DirectChat dc ON dc.chatRoom.id = r.roomId
        LEFT JOIN User otherU ON (
            (dc.userA.id = :userId AND otherU.id = dc.userB.id) OR 
            (dc.userB.id = :userId AND otherU.id = dc.userA.id)
        )
        WHERE ru.user.id = :userId
    """)
    Page<ChatRoomListItemResponse> findMyRoomListDto(@Param("userId") Long userId, Pageable pageable);
}