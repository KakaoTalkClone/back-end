package com.ocean.piuda.ai.groq.dto.request;

import jakarta.validation.constraints.*;
import lombok.Builder;

@Builder
public record GroqPromptRequest(

        @NotBlank(message = "prompt는 비어 있을 수 없습니다.")
        String prompt,          // 필수: 사용자 프롬프트

        @DecimalMin(value = "0.0", inclusive = true, message = "temperature는 0.0 이상이어야 합니다.")
        @DecimalMax(value = "2.0", inclusive = true, message = "temperature는 2.0 이하여야 합니다.")
        Double temperature,     // 선택: 샘플링 온도 (0.0 ~ 2.0 권장)

        @Positive(message = "maxTokens는 1 이상의 정수여야 합니다.")
        Integer maxTokens,      // 선택: 최대 토큰

        // 선택: 기본 false. true면 groq/compound + Web Search 사용
        Boolean useRealtime

) { }
