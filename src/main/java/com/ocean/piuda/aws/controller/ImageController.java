package com.ocean.piuda.aws.controller;



import com.ocean.piuda.aws.dto.response.GeneratePresignedPutUrlResponse;
import com.ocean.piuda.aws.utils.S3Utils;
import com.ocean.piuda.global.api.dto.ApiData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/image")
@RestController
@RequiredArgsConstructor
public class ImageController {

    private final S3Utils s3Utils;

    @GetMapping("/presigned-put-url")
    public ApiData<GeneratePresignedPutUrlResponse> generatePresignedPutUrl(
            @RequestParam String extension
    ) {
        return ApiData.ok(s3Utils.generatePresignedPutUrl(extension));
    }
}
