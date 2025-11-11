package com.ocean.piuda.opensource;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "direct_chat")
public class DirectChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "direct_chat_id")
    private Long directChatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_small_id", nullable = false)
    private Member memberSmall;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_big_id", nullable = false)
    private Member memberBig;

    @OneToOne
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom room;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    // getters, setters, constructors
}
