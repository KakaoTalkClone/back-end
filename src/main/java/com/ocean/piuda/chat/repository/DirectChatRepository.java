package com.ocean.piuda.chat.repository;

import com.ocean.piuda.chat.entity.DirectChat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DirectChatRepository extends JpaRepository<DirectChat, Long> {

    Optional<DirectChat> findByUserAIdAndUserBId(Long userAId, Long userBId);
}
