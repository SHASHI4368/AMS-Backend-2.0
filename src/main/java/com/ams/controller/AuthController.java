package com.ams.controller;

import com.ams.dto.*;
import com.ams.dto.auth.AuthRequest;
import com.ams.dto.auth.LoginResponse;
import com.ams.dto.auth.SignupResponse;
import com.ams.dto.auth.VerifyEmailRequest;
import com.ams.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        ResponseCookie cookie = ResponseCookie
                .from("jwt", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie.toString()
        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Logout successful",
                        null
                )
        );
    }
}
