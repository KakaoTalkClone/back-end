package com.ocean.piuda.chatRoom.entity;

import com.ocean.piuda.global.api.domain.BaseEntity;
import com.ocean.piuda.user.entity.RoomUser;
import com.ocean.piuda.chat.entity.Message;
import com.ocean.piuda.chatRoom.enums.RoomState;
import com.ocean.piuda.chatRoom.enums.RoomType;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "room")
public class Room extends BaseEntity {

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
    private RoomType roomType;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_state", nullable = false)
    private RoomState roomState = RoomState.ACTIVE;


    @OneToMany(mappedBy = "room")
    private List<Message> messages;

    @OneToMany(mappedBy = "room")
    private List<RoomUser> participants;
}
