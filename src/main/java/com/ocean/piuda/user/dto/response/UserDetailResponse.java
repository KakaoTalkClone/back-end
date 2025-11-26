package com.ocean.piuda.user.dto.response;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailResponse {

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/image.jpg")
    private String profileImageUrl;

    @NotNull
    @Schema(description = "프로필 바깥의 바깥 이미지")
    private List<String> backgroundImageUrls;

    @NotNull
    @Schema(description = "닉네임", example = "홍길동")
    private String nickname;

    @Schema(description = "상태 메시지", example = "오늘도 운동 중")
    private String statusMessage;
}