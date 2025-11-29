package com.ocean.piuda.chat.dto.response;

import lombok.Builder;

@Builder
public record ChatReadResponse(
        Long roomId,
        Long userId, // 누가 읽었는지
        Long messageId // 어디까지 읽었는지
) {}