package com.ocean.piuda.chatRoom.controller;

import com.ocean.piuda.chat.dto.response.ChatMessageResponse;
import com.ocean.piuda.chatRoom.dto.response.ChatRoomListItemResponse;
import com.ocean.piuda.chatRoom.dto.response.ChatRoomResponse;
import com.ocean.piuda.chat.service.ChatService;
import com.ocean.piuda.global.api.dto.ApiData;
import com.ocean.piuda.global.api.dto.PageResponse;
import com.ocean.piuda.security.jwt.service.TokenUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "Chat Room API", description = "채팅방 생성/조회/초대/나가기 API")
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatService chatService;
    private final TokenUserService tokenUserService;
    private final com.ocean.piuda.chatRoom.service.ChatRoomQueryService chatRoomQueryService;

    @PostMapping("/rooms/direct")
    @Operation(summary = "1:1 채팅방 생성/획득", description = "상대 userId로 1:1 채팅방을 생성하거나 기존 방을 반환합니다.")
    public ApiData<ChatRoomResponse> createDirectRoom(@RequestParam Long targetUserId) {
        Long meId = tokenUserService.getCurrentUser().getId();
        return ApiData.ok(chatService.ensureDirectRoom(meId, targetUserId));
    }

    @PostMapping("/rooms/group")
    @Operation(summary = "그룹 채팅방 생성", description = "그룹 채팅방을 생성하고 참여자를 등록합니다. (요청자 본인 자동 포함)")
    public ApiData<ChatRoomResponse> createGroupRoom(
            @RequestParam String roomName,
            @RequestBody List<Long> participantUserIds
    ) {
        Long meId = tokenUserService.getCurrentUser().getId();

        // 입력받은 리스트가 불변(Immutable)일 수 있으므로 새 리스트로 복사
        List<Long> finalParticipants = new ArrayList<>(participantUserIds);

        // 본인이 리스트에 없으면 추가 (방장은 무조건 참여)
        if (!finalParticipants.contains(meId)) {
            finalParticipants.add(meId);
        }

        return ApiData.ok(chatService.createGroupRoom(roomName, finalParticipants));
    }

    @PostMapping("/rooms/{roomId}/invite")
    @Operation(summary = "그룹방 초대", description = "기존 채팅방에 새로운 유저들을 초대합니다.")
    public ApiData<Void> inviteUsers(
            @PathVariable Long roomId,
            @RequestBody List<Long> targetUserIds
    ) {
        Long meId = tokenUserService.getCurrentUser().getId();
        chatService.inviteUsersToRoom(roomId, meId, targetUserIds);
        return ApiData.ok(null);
    }

    @PostMapping("/rooms/{roomId}/leave")
    @Operation(summary = "방 나가기", description = "해당 채팅방에서 나갑니다.")
    public ApiData<Void> leaveRoom(@PathVariable Long roomId) {
        Long meId = tokenUserService.getCurrentUser().getId();
        chatService.leaveRoom(roomId, meId);
        return ApiData.ok(null);
    }

    @GetMapping("/rooms")
    @Operation(summary = "내 채팅방 목록 조회", description = "현재 로그인한 사용자가 참여 중인 채팅방 목록을 조회합니다.")
    public ApiData<PageResponse<ChatRoomListItemResponse>> getMyRooms(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long meId = tokenUserService.getCurrentUser().getId();
        Page<ChatRoomListItemResponse> result =
                chatRoomQueryService.getMyRooms(meId, PageRequest.of(page - 1, size));
        return ApiData.ok(PageResponse.of(result));
    }

    @GetMapping("/rooms/{roomId}/messages")
    @Operation(summary = "채팅방 메시지 조회", description = "특정 채팅방의 메시지 목록을 최신 순으로 페이지 조회합니다. 조회와 동시에 읽음 처리됩니다.")
    public ApiData<PageResponse<ChatMessageResponse>> getRoomMessages(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long meId = tokenUserService.getCurrentUser().getId();
        Page<ChatMessageResponse> result =
                chatService.getMessages(roomId, meId, page - 1, size);
        return ApiData.ok(PageResponse.of(result));
    }
}