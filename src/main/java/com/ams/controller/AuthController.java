package com.ams.controller;

import com.ams.dto.*;
import com.ams.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
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
    public ResponseEntity<ApiResponse<SignupResponse>> signUp(
            @Valid
            @RequestBody
            AuthRequest authRequest
    ) {
        authService.signup(authRequest);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Signup successfull. Please check your email for the verification code",
                        new SignupResponse(authRequest.email())
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

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid
            @RequestBody
            AuthRequest authRequest,
            HttpServletResponse response
    ) {
        LoginResponse loginResponse = authService.login(authRequest, response);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Login successful",
                        loginResponse
                )
        );
    }
}
