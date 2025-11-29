package com.ocean.piuda.chat.repository;

import com.ocean.piuda.chat.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findByChatRoomRoomIdOrderByMessageIdDesc(Long roomId, Pageable pageable);

    /**
     * 특정 메시지 ID보다 큰(= 아직 읽지 않은) 메시지 개수
     */
    long countByChatRoomRoomIdAndMessageIdGreaterThan(Long roomId, Long messageId);
}