package com.ams.service;

import com.ams.entity.Organization;
import com.ams.entity.User;
import jakarta.mail.MessagingException;

public interface IEmailService {
    void sendVerificationEmail(String to, String code) throws MessagingException;
    void sendOrganizationJoinRequestEmail(
            String ownerEmail,
            User requestor,
            String note,
            Organization organization,
            String acceptToken,
            String rejectToken
    ) throws MessagingException;
}
