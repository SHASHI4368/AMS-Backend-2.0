package com.ams.controller;

import com.ams.dto.ApiResponse;
import com.ams.dto.LoginResponse;
import com.ams.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.base-path}/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<LoginResponse>> getCurrentUser(
            Authentication authentication
    ) {
        System.out.println(authentication.getName());
        LoginResponse loginResponse = userService.getCurrentUser(authentication.getName());

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Current user fetched successfully",
                        loginResponse
                )
        );
    }
}
