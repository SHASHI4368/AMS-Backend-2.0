package com.ams.service;

import jakarta.mail.MessagingException;

public interface IEmailService {
    void sendVerificationEmail(String to, String code) throws MessagingException;
}
