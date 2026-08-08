package com.ams.service;

import com.ams.dto.SignUpRequest;
import com.ams.dto.VerifyEmailRequest;

public interface IAuthService {
    void signup(SignUpRequest signUpRequest);
    void verifyEmail(VerifyEmailRequest verifyEmailRequest);
}
