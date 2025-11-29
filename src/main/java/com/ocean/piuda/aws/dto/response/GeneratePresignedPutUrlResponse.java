package com.ocean.piuda.aws.dto.response;

import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.time.Instant;

public record GeneratePresignedPutUrlResponse(
        String uploadUrl,
        long expirationSeconds,
        String key
) {
  public static GeneratePresignedPutUrlResponse from(PresignedPutObjectRequest req, String key) {
    long seconds = Duration.between(Instant.now(), req.expiration()).getSeconds();
    return new GeneratePresignedPutUrlResponse(req.url().toString(), seconds, key);
  }
}
