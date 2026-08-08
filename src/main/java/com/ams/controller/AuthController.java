package com.ams.controller;

import com.ams.dto.ApiResponse;
import com.ams.dto.SignUpRequest;
import com.ams.dto.VerifyEmailRequest;
import com.ams.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.base-path}/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signUp(
            @Valid
            @RequestBody
            SignUpRequest signUpRequest
    ) {
        authService.signup(signUpRequest);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Signup successfull. Please check your email for the verification code",
                        null
                )
        );
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @Valid
            @RequestBody
            VerifyEmailRequest verifyEmailRequest
    ) {
        authService.verifyEmail(verifyEmailRequest);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Email verified successfully",
                        null
                )
        );
    }
}
