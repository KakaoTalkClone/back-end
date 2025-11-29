package com.ocean.piuda.chatRoom.repository;

import com.ocean.piuda.chatRoom.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<ChatRoom, Long> {
}
