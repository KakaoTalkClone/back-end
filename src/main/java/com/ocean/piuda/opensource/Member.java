package com.ocean.piuda.opensource;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "member")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(length = 255)
    private String email;

    @Column(length = 2048)
    private String fcmToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_platform")
    private DevicePlatform devicePlatform = DevicePlatform.ANDROID;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "member")
    private List<ChatPart> chatParts;

    @OneToMany(mappedBy = "sender")
    private List<Message> messages;

    // getters, setters, constructors
}