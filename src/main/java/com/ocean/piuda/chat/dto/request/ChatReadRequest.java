package com.ocean.piuda.chat.dto.request;

public record ChatReadRequest(
        Long roomId,
        Long messageId // 어디까지 읽었는지 (해당 메시지 ID 포함, 그 이전 메시지들은 다 읽은 것으로 처리)
) {}