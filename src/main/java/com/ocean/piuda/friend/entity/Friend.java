package com.ocean.piuda.friend.entity;

import com.ocean.piuda.global.api.domain.BaseEntity;
import com.ocean.piuda.friend.enums.FriendStatus;
import com.ocean.piuda.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table( name = "friend",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "friend_user_id"})
        }
)
public class Friend extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "friend_id")
    private Long friendId;

    /**
     * 유저 본인
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 유저가 친구로 등록한 상대
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_user_id", nullable = false)
    private User friendUser;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendStatus status = FriendStatus.ACTIVE;


    public static Friend of(User user, User friendUser) {
        return Friend.builder()
                .user(user)
                .friendUser(friendUser)
                .status(FriendStatus.ACTIVE)
                .build();
    }

}
