package com.ocean.piuda.chatRoom.entity;

import com.ocean.piuda.chatRoom.enums.ChatRoomType;
import com.ocean.piuda.global.api.domain.BaseEntity;
import com.ocean.piuda.user.entity.RoomUser;
import com.ocean.piuda.chat.entity.Message;
import com.ocean.piuda.chatRoom.enums.ChatRoomState;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "room")
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    @OneToOne
    @JoinColumn(name = "last_message_id")
    private Message lastMessage;

    @Column(name = "room_name", length = 100)
    private String roomName;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false)
    private ChatRoomType chatRoomType;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_state", nullable = false)
    @Builder.Default
    private ChatRoomState chatRoomState = ChatRoomState.ACTIVE;   // 기본값

    @OneToMany(mappedBy = "chatRoom")
    private List<Message> messages;

    @OneToMany(mappedBy = "chatRoom")
    private List<RoomUser> participants;

    public void updateLastMessage(Message message) {
        this.lastMessage = message;
    }
}