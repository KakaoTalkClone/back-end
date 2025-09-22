package com.ocean.piuda.aws.utils;

import com.ocean.piuda.aws.dto.response.GeneratePresignedPutUrlResponse;
import com.ocean.piuda.aws.properties.AwsConfigProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class S3Utils {

    private final S3Presigner s3Presigner;
    private final AwsConfigProperties awsConfigProperties;


    public GeneratePresignedPutUrlResponse generatePresignedPutUrl(String extension) {
        String key = this.getRandomFilename(this.awsConfigProperties.getS3().getUserObjectsDirectory(), extension);

        return this.generatePresignedPutUrl(
                this.awsConfigProperties.getS3().getBucketName(),
                key,
                Duration.ofSeconds(this.awsConfigProperties.getS3().getExpirationTime())
        );
    }

    public GeneratePresignedPutUrlResponse generatePresignedPutUrl(
            String bucketName,
            String key,
            Duration expirationTime
    ) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        PutObjectPresignRequest putObjectPresignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(expirationTime)
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedPutObjectRequest = s3Presigner.presignPutObject(
                putObjectPresignRequest);

        return GeneratePresignedPutUrlResponse.from(presignedPutObjectRequest, key);
    }

    public String getRandomFilename(String folder, String extension) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String timestamp = String.valueOf(Instant.now().toEpochMilli());

        return String.format("%s_%s%s", uuid, timestamp, extension);
    }
}
