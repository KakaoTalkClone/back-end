package com.ocean.piuda.chat.entity;

import com.ocean.piuda.global.api.domain.BaseEntity;
import com.ocean.piuda.chatRoom.entity.Room;
import com.ocean.piuda.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


/**
 * 두 유저 간의 1:1 채팅방의 유일성 보장 및 빠른 조회를 위한 제약 엔티티
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "direct_chat",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"userA_id", "userB_id"})
}
)
public class DirectChat extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "direct_chat_id")
    private Long directChatId;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userA_id", nullable = false)
    private User userA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userB_id", nullable = false)
    private User userB;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false, unique = true)
    private Room room;

    /**
     * 주의 :
     * ( userA, userB ) 와 ( userB, userA ) 는 다른 순열로 인식되기에
     * 내부적으로 항상 userA 를 id값 더 작은쪽, userB 를 id 값 더 큰 유저를 배치하여
     * unique 조건을 만족시켜야된다.
     * 이를 위해 이 엔티티에 정의한 of 를 활용한 생성 수행 권장.
     */
    public static DirectChat of(User u1, User u2, Room room) {
        User min = u1.getId() < u2.getId() ? u1 : u2;
        User max = u1.getId() < u2.getId() ? u2 : u1;

        return DirectChat.builder()
                .userA(min)
                .userB(max)
                .room(room)
                .build();
    }


}
