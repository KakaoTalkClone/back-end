package com.ocean.piuda.image.entity;

import com.ocean.piuda.global.api.domain.BaseEntity;
import com.ocean.piuda.image.enums.MemberImageType;
import com.ocean.piuda.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_image",
       indexes = {
           @Index(name = "idx_member_image_user", columnList = "user_id"),
           @Index(name = "idx_member_image_image", columnList = "image_id")
       })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_member_image_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_member_image_image"))
    private Image image;

    @Column(name = "type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private MemberImageType type;

}