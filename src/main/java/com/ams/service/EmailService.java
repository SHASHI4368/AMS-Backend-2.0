package com.ams.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService implements  IEmailService {
    private final JavaMailSender mailSender;
    @Override
    public void sendVerificationEmail(String to, String code) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper =
                new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject("Verify your email - Appointment Management System");

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

        helper.setText(html, true);

        mailSender.send(message);
    }
}
