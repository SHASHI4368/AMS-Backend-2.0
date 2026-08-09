package com.ams.service;

import com.ams.dto.AuthRequest;
import com.ams.dto.LoginResponse;
import com.ams.dto.VerifyEmailRequest;
import com.ams.entity.EmailVerificationCode;
import com.ams.entity.User;
import com.ams.exception.ServiceException;
import com.ams.repository.EmailVerificationCodeRepository;
import com.ams.repository.UserRepository;
import com.ams.role.Role;
import com.ams.util.JwtUtil;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {
    private final UserRepository userRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    private final EmailVerificationCodeRepository emailVerificationCodeRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public void signup(AuthRequest authRequest) {
        // 1. Check if email already exists
        if (userRepository.existsByEmail(authRequest.email())) {
            throw new ServiceException("Email already exists");
        }

        // 2. create new user
        User user = User.builder()
                .email(authRequest.email())
                .password(passwordEncoder.encode(authRequest.password()))
                .role(Role.USER)
                .emailVerified(false)
                .build();

        // 3. save user to database
        userRepository.save(user);

        // 4. Generate verification code
        String verificationCode = generateVerificationCode();

        // 5. Create a verification record
        EmailVerificationCode verificationRecord = EmailVerificationCode.builder()
                .user(user)
                .code(passwordEncoder.encode(verificationCode))
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(10)) // Code expires in 10 minutes
                .build();

        // 6. Save verification record to database
        emailVerificationCodeRepository.save(verificationRecord);

        // 7. Send email
        try{
            emailService.sendVerificationEmail(
                    user.getEmail(),
                    verificationCode
            );
        }catch (MessagingException e){
            throw new ServiceException("Failed to send verification email: " + e.getMessage());
        }
    }

    private String generateVerificationCode() {
        // Generate a random verification code
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }

    @Override
    public void verifyEmail(VerifyEmailRequest verifyEmailRequest) {
        // 1. Find user
        User user = userRepository
                .findByEmail(verifyEmailRequest.email())
                .orElseThrow(() ->
                        new ServiceException("User not found")
                );

        // 2. Check if user is already verified
        if (user.isEmailVerified()) {
            throw new ServiceException("Email is already verified");
        }

        // 3. Find verification record
        EmailVerificationCode verificationRecord = emailVerificationCodeRepository
                .findByUser(user)
                .orElseThrow(() ->
                        new ServiceException("Verification record not found")
                );

        // 4. Check expiration
        if (verificationRecord.getExpiresAt().isBefore(LocalDateTime.now())) {
            emailVerificationCodeRepository.delete(verificationRecord);
            throw new ServiceException("Verification code has expired");
        }

        // 5. Check code
        if (!passwordEncoder.matches(
                verifyEmailRequest.code(),
                verificationRecord.getCode()
        )) {
            throw new ServiceException("Invalid verification code");
        }

        // 6. Mark user as verified
        user.setEmailVerified(true);
        userRepository.save(user);

        // 7. Delete verification record
        emailVerificationCodeRepository.delete(verificationRecord);

    }

    @Override
    public LoginResponse login(AuthRequest authRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.email(),
                        authRequest.password()
                )
        );
        User user = userRepository
                .findByEmail(authRequest.email())
                .orElseThrow(() ->
                        new ServiceException("User not found")
                );
        String token = jwtUtil.generateJwtToken(user.getEmail());
        return new LoginResponse(
                token,
                user.getEmail(),
                user.getRole().name()
        );
    }
}
