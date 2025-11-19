package com.ocean.piuda.user.entity;



import com.ocean.piuda.device.enums.DevicePlatform;
import com.ocean.piuda.global.api.domain.BaseEntity;
import com.ocean.piuda.chat.entity.Message;
import com.ocean.piuda.security.jwt.dto.request.UserUpdateRequestDto;
import com.ocean.piuda.security.jwt.enums.Role;
import com.ocean.piuda.security.oauth2.enums.ProviderType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Table(name = "users")
@Entity
@Getter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class User extends BaseEntity {

    /**
     * 우리 애플리케이션 상의 (물리적) 식별자값
     * 자체로그인, OAuth2 공통
     */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    /**
     * OAuth2 provider 벤더명 (KAKAO, NAVER, GOOGLE)
     * 자체 로그인일 경우 null
     */
    @Enumerated(EnumType.STRING)
    private ProviderType providerType;

    /**
     * OAuth2 provider 상의 식별자 값
     * 자체 로그인일 경우 null
     */
    private String providerId;



    /**
     * 자체로그인 논리적 식별자값
     * OAuth2 로그인일경우 null
     */
    @Column(unique = true)
    private String username;

    /**
     * 자체로그인 비밀번호
     * OAuth2 로그인일경우 null
     */
    @Column
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;


    /**
     * 부가적인 정보들
     */
    // 닉네임. 사용자명
    private String nickname;
    private String email;
    private String phone;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "device_platform")
    private DevicePlatform devicePlatform = DevicePlatform.ANDROID;


    @OneToMany(mappedBy = "user")
    private List<RoomUser> roomUsers;

    @OneToMany(mappedBy = "sender")
    private List<Message> messages;


    public void updateRole(Role role){
        this.role = role;
    }


    public void update(UserUpdateRequestDto dto) {
        if (dto.getNickname() != null && !dto.getNickname().isBlank()) {
            this.nickname = dto.getNickname();
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            this.email = dto.getEmail();
        }

        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            this.phone = dto.getPhone();
        }

    }


}
