package com.ocean.piuda.test;


import com.ocean.piuda.global.api.dto.ApiData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class TestController {


    @GetMapping("/test")
    public ApiData<?> createMenu(

    ) {
        return ApiData.created("hello");
    }
}
