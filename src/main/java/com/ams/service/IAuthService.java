package com.ams.service;

import com.ams.dto.auth.AuthRequest;
import com.ams.dto.auth.LoginResponse;
import com.ams.dto.auth.VerifyEmailRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface IAuthService {
    void signup(AuthRequest authRequest);
    void verifyEmail(VerifyEmailRequest verifyEmailRequest);
    LoginResponse login(AuthRequest authRequest, HttpServletResponse response);
}
