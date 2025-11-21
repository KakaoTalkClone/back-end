package com.ocean.piuda.user.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

    @NotNull
    private Long userId;

    @NotNull
    private String nickname;

    @NotNull
    private String phone;
}
