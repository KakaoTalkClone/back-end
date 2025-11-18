package com.ocean.piuda.user.entity;

import com.ocean.piuda.global.api.domain.BaseEntity;
import com.ocean.piuda.chat.entity.Message;
import com.ocean.piuda.chatRoom.entity.Room;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;


/**
 * ChatRoom 과 User 의 중간테이블 및 유저 별 방의 메타 데이터 저장용
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "room_user")
public class RoomUser extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_user_id")
    private Long roomUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_read_message_id")
    private Message lastReadMessage;

    @Builder.Default
    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt = LocalDateTime.now();

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;



    // getters, setters, constructors
}