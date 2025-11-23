package com.ocean.piuda.user.condition;


import lombok.Getter;
import lombok.Setter;
import org.springdoc.core.annotations.ParameterObject;
import io.swagger.v3.oas.annotations.media.Schema;


@Getter
@Setter
@ParameterObject
public class UserListCondition {
    private int page = 1;

    private int size = 10;

    @Schema(description = "조회할 회원 username (null이면 전체 조회)", example = "john")
    private String username;
}
