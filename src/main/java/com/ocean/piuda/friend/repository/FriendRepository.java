package com.ocean.piuda.friend.repository;

import com.ocean.piuda.friend.dto.response.FriendDetailResponse;
import com.ocean.piuda.friend.dto.response.FriendResponse;
import com.ocean.piuda.friend.entity.Friend;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {


    @Query("""

    select new com.ocean.piuda.friend.dto.response.FriendResponse(
         u.id,
         i.url,
         u.nickname,
         u.statusMessage
     )
     from Friend f
     join f.friendUser u
     left join UserImage ui on ui.user = u and ui.type = com.ocean.piuda.image.enums.UserImageType.PROFILE
     left join Image i on i.id = ui.image.id
     where f.user.id = :userId
    """)
    Page<FriendResponse> findFriendByPage(Long userId, Pageable pageable);


//    @Schema(description = "프로필 이미지 URL", example = "https://example.com/image.jpg")
//    private String profileImageUrl;
//
//    @NotNull
//    @Schema(description = "프로필 바깥의 바깥 이미지")
//    private List<String> backgroundImageUrls;
//
//    @NotNull
//    @Schema(description = "닉네임", example = "홍길동")
//    private String nickname;
//
//    @Schema(description = "상태 메시지", example = "오늘도 운동 중")
//    private String statusMessage;

    @Query("""

    select new com.ocean.piuda.friend.dto.response.FriendDetailResponse(
         i.url,
         null,
         f.nickname,
         u.statusMessage
     )
     from Friend f
     join f.friendUser u
     left join UserImage ui on ui.user = u and ui.type = com.ocean.piuda.image.enums.UserImageType.PROFILE
     left join Image i on i.id = ui.image.id
     where f.user.id = :meUserId
     and f.friendUser.id = :targetUserId
    """)
    Optional<FriendDetailResponse> findFriendDetail(
            @Param("meUserId") Long meUserId,
            @Param("targetUserId")Long targetUserId
    );


    Optional<Friend> findByUserIdAndFriendUserId(Long userId, Long friendUserId);

}
