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

    /**
     * [N+1 문제 해결]
     * 내 채팅방 목록 + 마지막 메시지 + 안 읽은 개수를 '한 번의 쿼리'로 조회
     *
     * JPQL의 SELECT절 서브쿼리를 사용하여 각 방(Row)마다 안 읽은 메시지 수를 즉시 계산합니다.
     */
    @Query("""
        SELECT new com.ocean.piuda.chatRoom.dto.response.ChatRoomListItemResponse(
            r.roomId,
            r.roomName,
            r.chatRoomType,
            lm.content,
            lm.createdAt,
            (
                SELECT count(m)
                FROM Message m
                WHERE m.chatRoom.roomId = r.roomId
                  AND m.messageId > COALESCE(ru.lastReadMessage.messageId, 0)
            )
        )
        FROM RoomUser ru
        JOIN ru.chatRoom r
        LEFT JOIN r.lastMessage lm
        WHERE ru.user.id = :userId
    """)
    Page<ChatRoomListItemResponse> findMyRoomListDto(@Param("userId") Long userId, Pageable pageable);

}