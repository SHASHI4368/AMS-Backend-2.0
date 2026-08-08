package com.ams.controller;

import com.ams.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.base-path}/auth")
public class AuthController {

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signUp() {
        ApiResponse<Void> response = new ApiResponse<>(true, "User registered successfully", null);
        return ResponseEntity.ok(response);
    }
}
