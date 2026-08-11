package com.ams.service;

import com.ams.dto.AuthRequest;
import com.ams.dto.LoginResponse;
import com.ams.dto.VerifyEmailRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface IAuthService {
    void signup(AuthRequest authRequest);
    void verifyEmail(VerifyEmailRequest verifyEmailRequest);
    LoginResponse login(AuthRequest authRequest, HttpServletResponse response);
}
