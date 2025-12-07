package com.ocean.piuda.friend.controller;


import com.ocean.piuda.friend.dto.request.FriendPhoneRequest;
import com.ocean.piuda.friend.dto.request.FriendUsernameRequest;
import com.ocean.piuda.friend.dto.response.FriendResponse;
import com.ocean.piuda.friend.service.FriendService;
import com.ocean.piuda.global.api.dto.ApiData;
import com.ocean.piuda.global.api.dto.PageResponse;
import com.ocean.piuda.global.util.SecurityUtil;
import com.ocean.piuda.user.service.UserCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Friend API", description = "친구 API")
@RestController
@RequestMapping("/api/friend")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;
    private final SecurityUtil securityUtil;

    @GetMapping("")
    @Operation(summary = "친구 목록 조회", description = "size, page를 통해 친구 목록을 조회합니다.")
    public ApiData<PageResponse<FriendResponse>> getFriendList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String nickname
    ) {

        Pageable pageable = PageRequest.of(page - 1, size);

        Page<FriendResponse> response = friendService.findFriendFrom(securityUtil.getUserId(), nickname, pageable);

        return ApiData.ok(PageResponse.of(response));
    }

    @PostMapping("/phone")
    @Operation(summary = "연락처로 친구 추가", description = "닉네임과 전화번호로 친구를 추가합니다.")
    public ApiData<String> addFriendByPhone(@RequestBody FriendPhoneRequest request) {
        friendService.addFriendFrom(securityUtil.getUserId(), request);
        return ApiData.ok("ok");
    }

    @PostMapping("/username")
    @Operation(summary = "카카오톡 ID로 친구 추가", description = "username으로 친구를 추가합니다.")
    public ApiData<String> addFriendByUsername(@RequestBody FriendUsernameRequest request) {
        friendService.addFriendFrom(securityUtil.getUserId(), request);
        return ApiData.ok("ok");
    }


}
