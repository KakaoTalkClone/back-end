package com.ocean.piuda.user.controller;

import com.ocean.piuda.friend.controller.FriendController;
import com.ocean.piuda.global.api.dto.ApiData;
import com.ocean.piuda.security.jwt.dto.request.UserUpdateRequestDto;
import com.ocean.piuda.security.jwt.service.TokenUserService;
import com.ocean.piuda.user.dto.request.UserSearchRequest;
import com.ocean.piuda.user.dto.response.DetailedUserResponse;
import com.ocean.piuda.user.dto.response.UserResponse;
import com.ocean.piuda.user.service.UserAggregateBuilder;
import com.ocean.piuda.user.service.UserCommandService;
import com.ocean.piuda.user.service.UserQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "유저 API", description = "유저 정보 조회, 검색, 수정 기능")
public class UserController {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;
    private final TokenUserService tokenUserService;
    private final UserAggregateBuilder aggregateBuilder;

    @PatchMapping("/{userId}")
    @Operation(summary = "유저 정보 수정", description = "특정 유저의 닉네임, 이메일, 전화번호 등 기본 정보를 수정합니다.")
    public ApiData<Boolean> patchUser(
            @PathVariable Long userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "수정할 유저 정보", required = true)
            @RequestBody @Valid UserUpdateRequestDto req
    ) {
        userCommandService.patch(userId, req);
        return ApiData.ok(true);
    }

    @PostMapping("/search")
    @Operation(summary = "유저 검색", description = "닉네임 또는 username을 기준으로 유저를 검색합니다.")
    public ApiData<Page<DetailedUserResponse>> searchByNicknameOrUsername(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "검색 요청 정보", required = true)
            @RequestBody @Valid UserSearchRequest req
    ) {
        return ApiData.ok(userQueryService.searchByNicknameOrUsername(req));
    }

    @GetMapping("/{userId}/info")
    @Operation(summary = "유저 상세 조회", description = "id 기반으로 특정 유저의 상세 정보를 조회합니다.")
    public ApiData<DetailedUserResponse> getUserById(@PathVariable Long userId) {
        return ApiData.ok(aggregateBuilder.build(userQueryService.getUserById(userId)));
    }

    @GetMapping("/my/info")
    @Operation(summary = "내 정보 조회", description = "Access Token 기반으로 현재 로그인한 유저의 상세 정보를 조회합니다.")
    public ApiData<DetailedUserResponse> getLoginedUser() {
        return ApiData.ok(aggregateBuilder.build(tokenUserService.getCurrentUser()));
    }



    @GetMapping("")
    @Operation(summary = "회원 조회", description = "카카오톡 ID로 추가 -> username으로 해당 회원 조회. 추천친구 -> username비우고 page, size로 각 회원들 조회")
    public ApiData<List<UserResponse>> searchUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String username
    ) {
        List<UserResponse> dummyList = List.of(
                new UserResponse(1L, "홍길동", "010-1234-5678"),
                new UserResponse(2L, "김철수", "010-9876-5432")
        );
        return ApiData.ok(dummyList);
    }

}
