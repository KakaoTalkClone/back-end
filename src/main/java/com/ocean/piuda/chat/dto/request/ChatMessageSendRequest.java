package com.ocean.piuda.chat.dto.request;

import com.ocean.piuda.chat.enums.ContentType;

public record ChatMessageSendRequest(
        Long roomId,
        String content,
        ContentType contentType
) {}