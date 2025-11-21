package com.ocean.piuda.friend.controller;


import com.ocean.piuda.friend.dto.request.FriendPhoneRequest;
import com.ocean.piuda.friend.dto.request.FriendUsernameRequest;
import com.ocean.piuda.friend.dto.response.FriendDetailResponse;
import com.ocean.piuda.friend.dto.response.FriendResponse;
import com.ocean.piuda.global.api.dto.ApiData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Friend API", description = "친구 API")
@RestController
@RequestMapping("/api/friend")
@RequiredArgsConstructor
public class FriendController {


    @GetMapping("")
    @Operation(summary = "친구 목록 조회", description = "size, page를 통해 친구 목록을 조회합니다.")
    public ApiData<List<FriendResponse>> getFriendList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<FriendResponse> dummy = List.of(
                new FriendResponse(1L, "https://example.com/image1.jpg", "홍길동", "안녕하세요!"),
                new FriendResponse(2L, "https://example.com/image2.jpg", "김철수", "헬스 중")
        );
        return ApiData.ok(dummy);
    }

    @PostMapping("/phone")
    @Operation(summary = "연락처로 친구 추가", description = "닉네임과 전화번호로 친구를 추가합니다.")
    public ApiData<Void> addFriendByPhone(@RequestBody FriendPhoneRequest request) {
        // TODO: 실제 서비스 로직 연결
        return ApiData.ok(null);
    }

    @PostMapping("/username")
    @Operation(summary = "카카오톡 ID로 친구 추가", description = "username으로 친구를 추가합니다.")
    public ApiData<Void> addFriendByUsername(@RequestBody FriendUsernameRequest request) {
        // TODO: 실제 서비스 로직 연결
        return ApiData.ok(null);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "친구 프로필 조회", description = "userId로 친구 프로필을 조회합니다.")
    public ApiData<FriendDetailResponse> getFriendProfile(@PathVariable Long userId) {
        FriendDetailResponse dummy = new FriendDetailResponse(
                "https://example.com/image.jpg",
                List.of(
                        "https://example.com/bg1.jpg",
                        "https://example.com/bg2.jpg"
                ),
                "홍길동",
                "오늘도 운동 중"
        );
        return ApiData.ok(dummy);
    }

}
