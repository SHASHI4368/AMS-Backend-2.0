package com.ams.service;

import com.ams.entity.Organization;
import com.ams.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

@Service
@RequiredArgsConstructor
public class EmailService implements  IEmailService {
    private final JavaMailSender mailSender;

    @Value("${api.backend-url}")
    private String backendUrl;

    private void sendEmail(
            String to,
            String subject,
            String htmlContent
    ) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper =
                new MimeMessageHelper(
                        message,
                        true,
                        "UTF-8"
                );

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }


    @Override
    public void sendVerificationEmail(String to, String code) throws MessagingException {

        String html = """
                <html>
                <body>
                    <h2>Verify your email</h2>

                    <p>
                        Thank you for signing up for the
                        Appointment Management System.
                    </p>

                    <p>Your verification code is:</p>

                    <h1>%s</h1>

                    <p>
                        This code will expire in 10 minutes.
                    </p>

                    <p>
                        If you did not create this account,
                        you can ignore this email.
                    </p>
                </body>
                </html>
                """.formatted(code);

        sendEmail(to, "\"Verify your email - Appointment Management System\"", html);

    }

    @Override
    public void sendOrganizationJoinRequestEmail(
            String ownerEmail,
            User requestor,
            String note,
            Organization organization,
            String acceptToken,
            String rejectToken
    ) throws MessagingException {

        String acceptUrl =
                backendUrl +
                        "/api/v1/organization-email-actions/accept?token=" +
                        acceptToken;

        String rejectUrl =
                backendUrl +
                        "/api/v1/organization-email-actions/reject?token=" +
                        rejectToken;

        /*
         * Only display the note if the requestor provided one.
         */
        String noteSection = "";

        if (note != null && !note.isBlank()) {

            String safeNote = HtmlUtils.htmlEscape(note);

            noteSection = """
    <div style="
        margin: 8px 0 24px 0;
        padding: 16px 18px;
        background-color: #f8f9fa;
        border-left: 3px solid #3b82f6;
        border-radius: 6px;
    ">
        <p style="margin:0 0 6px 0; font-weight:600; font-size:13px; color:#374151;">
            Message from the requestor
        </p>
        <p style="margin:0; color:#4b5563; font-size:14px; line-height:1.5;">
            %s
        </p>
    </div>
    """.formatted(safeNote);
        }

        String html = """
    <!DOCTYPE html>
    <html>
    <body style="margin:0; padding:0; background-color:#f4f5f7; font-family:'Segoe UI', Arial, sans-serif;">
        <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f4f5f7; padding:40px 0;">
            <tr>
                <td align="center">
                    <table role="presentation" width="600" cellpadding="0" cellspacing="0" style="background-color:#ffffff; border-radius:12px; overflow:hidden; box-shadow:0 1px 3px rgba(0,0,0,0.08);">

                        <tr>
                            <td style="background-color:#111827; padding:28px 40px;">
                                <span style="color:#ffffff; font-size:18px; font-weight:600;">Appointment Management System</span>
                            </td>
                        </tr>

                        <tr>
                            <td style="padding:40px;">
                                <h2 style="margin:0 0 16px 0; font-size:22px; color:#111827;">New Join Request</h2>

                                <p style="margin:0 0 20px 0; font-size:15px; color:#4b5563; line-height:1.6;">
                                    <strong style="color:#111827;">%s</strong> has requested to join your organization,
                                    <strong style="color:#111827;">%s</strong>.
                                </p>

                                %s

                                <table role="presentation" cellpadding="0" cellspacing="0" style="margin:32px 0;">
                                    <tr>
                                        <td style="padding-right:12px;">
                                            <a href="%s" style="display:inline-block; padding:13px 28px; background-color:#16a34a; color:#ffffff; text-decoration:none; border-radius:8px; font-size:14px; font-weight:600;">
                                                Accept Request
                                            </a>
                                        </td>
                                        <td>
                                            <a href="%s" style="display:inline-block; padding:13px 28px; background-color:#ffffff; color:#dc2626; text-decoration:none; border-radius:8px; font-size:14px; font-weight:600; border:1px solid #dc2626;">
                                                Reject Request
                                            </a>
                                        </td>
                                    </tr>
                                </table>

                                <p style="margin:0; font-size:13px; color:#9ca3af; line-height:1.5;">
                                    These links are valid for 24 hours and can only be used once.
                                    If you did not expect this request, you can safely ignore this email.
                                </p>
                            </td>
                        </tr>

                        <tr>
                            <td style="background-color:#f9fafb; padding:20px 40px; border-top:1px solid #eee;">
                                <p style="margin:0; font-size:12px; color:#9ca3af;">
                                    This is an automated message — please don't reply directly to this email.
                                </p>
                            </td>
                        </tr>

                    </table>
                </td>
            </tr>
        </table>
    </body>
    </html>
    """.formatted(
                requestor.getEmail(),
                organization.getName(),
                noteSection,
                acceptUrl,
                rejectUrl
        );

        sendEmail(ownerEmail, "New Join Request for " + organization.getName(), html);
    }

    @Override
    public void sendMembershipRequestResultEmail(
            String to,
            String organizationName,
            boolean accepted
    ) throws MessagingException {



        String subject;
        String title;
        String content;

        if (accepted) {

            subject =
                    "Your request to join "
                            + organizationName
                            + " was accepted";

            title = "Join request accepted";

            content = """
                <p>
                    Your request to join
                    <strong>%s</strong>
                    has been accepted.
                </p>

                <p>
                    You are now a member of the organization.
                </p>
                """.formatted(organizationName);

        } else {

            subject =
                    "Your request to join "
                            + organizationName
                            + " was rejected";

            title = "Join request rejected";

            content = """
                <p>
                    Your request to join
                    <strong>%s</strong>
                    has been rejected.
                </p>
                """.formatted(organizationName);
        }

        String html = """
            <!DOCTYPE html>
            <html>
            <body style="
                font-family: Arial, sans-serif;
                line-height: 1.6;
                color: #333;
            ">

                <div style="
                    max-width: 600px;
                    margin: 30px auto;
                    padding: 30px;
                    border: 1px solid #ddd;
                    border-radius: 8px;
                ">

                    <h2>%s</h2>

                    %s

                </div>

            </body>
            </html>
            """.formatted(title, content);

        sendEmail(to, subject, html);
    }
}
