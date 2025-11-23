package com.ocean.piuda.friend.service;


import com.ocean.piuda.friend.dto.request.FriendPhoneRequest;
import com.ocean.piuda.friend.dto.request.FriendUsernameRequest;
import com.ocean.piuda.friend.dto.response.FriendDetailResponse;
import com.ocean.piuda.friend.dto.response.FriendResponse;
import com.ocean.piuda.friend.entity.Friend;
import com.ocean.piuda.friend.repository.FriendRepository;
import com.ocean.piuda.global.api.exception.BusinessException;
import com.ocean.piuda.global.api.exception.ExceptionType;
import com.ocean.piuda.image.service.ImageService;
import com.ocean.piuda.user.entity.User;
import com.ocean.piuda.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class FriendService {

    private final FriendRepository friendRepository;
    private final UserQueryService userQueryService;
    private final ImageService imageService;


    @Transactional
    public Friend addFriendFrom(Long userId, FriendPhoneRequest request)  {
        User findMeUser = userQueryService.getUserById(userId);

        User findFriendTargetUser = userQueryService.findUserFromPhone(request.getPhone())
                .orElseThrow(() -> new BusinessException(ExceptionType.USER_NOT_FOUND));

        validateExistsFriend(userId, findFriendTargetUser);

         Friend friend = Friend.of(
             findMeUser,
             findFriendTargetUser,
             request.getNickname()
         );

         return friendRepository.save(friend);
    }

    @Transactional
    public Friend addFriendFrom(Long userId, FriendUsernameRequest request) {
        User findMeUser = userQueryService.getUserById(userId);

        User findFriendTargetUser = userQueryService.findUserFromUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(ExceptionType.USER_NOT_FOUND));

        validateExistsFriend(userId, findFriendTargetUser);

        Friend friend = Friend.of(
                findMeUser,
                findFriendTargetUser,
                findFriendTargetUser.getNickname()
        );

        return friendRepository.save(friend);
    }

    public Page<FriendResponse> findFriendFrom(Long userId, Pageable pageable) {
        return friendRepository.findFriendByPage(userId, pageable);
    }

    public Optional<Friend> findFriendFrom(Long userId, Long friendUserId){
        return friendRepository.findByUserIdAndFriendUserId(userId, friendUserId);
    }

    public FriendDetailResponse findFriendDetail(Long meUserId, Long targetUserId) {
        FriendDetailResponse response = friendRepository.findFriendDetail(meUserId, targetUserId)
                .orElseThrow(() -> new BusinessException(ExceptionType.FRIEND_NOT_FOUND));

        List<String> backgroundImageUrls = imageService.findBackgroundImages(targetUserId);

        response.setBackgroundImageUrls(backgroundImageUrls);

        return response;
    }

    private void validateExistsFriend(Long userId, User findUser) {
        findFriendFrom(userId, findUser.getId())
                .ifPresent(f -> {
                    throw new BusinessException(ExceptionType.ALREADY_FRIEND);
                });
    }

}
