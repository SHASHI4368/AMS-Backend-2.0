package com.ams.service;

import com.ams.dto.auth.AuthRequest;
import com.ams.dto.auth.LoginResponse;
import com.ams.dto.auth.VerifyEmailRequest;
import com.ams.entity.EmailVerificationCode;
import com.ams.entity.Profile;
import com.ams.entity.User;
import com.ams.exception.ServiceException;
import com.ams.repository.EmailVerificationCodeRepository;
import com.ams.repository.ProfileRepository;
import com.ams.repository.UserRepository;
import com.ams.enums.Role;
import com.ams.util.JwtUtil;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    private final ProfileRepository profileRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    private final EmailVerificationCodeRepository emailVerificationCodeRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Value("${jwt.expiration}")
    private int maxAge;

    @Override
    @Transactional
    public void signup(AuthRequest authRequest) {
        // 1. Check if email already exists and email is verified
        if (userRepository.existsByEmail(authRequest.email())) {
            User existingUser = userRepository.findByEmail(authRequest.email()).orElseThrow(
                    () -> new ServiceException("User not found")
            );
            if (existingUser.isEmailVerified()) {
                throw new ServiceException("Email already exists and is verified");
            }
            EmailVerificationCode existingVerificationRecord = emailVerificationCodeRepository
                    .findByUser(existingUser)
                    .orElseThrow(() ->
                            new ServiceException("Verification record not found")
                    );
            String newVerificationCode = generateVerificationCode();
            existingVerificationRecord.setCode(passwordEncoder.encode(newVerificationCode));
            existingVerificationRecord.setCreatedAt(LocalDateTime.now());
            existingVerificationRecord.setExpiresAt(LocalDateTime.now().plusMinutes(10));
            emailVerificationCodeRepository.save(existingVerificationRecord);
            try {
                emailService.sendVerificationEmail(
                        existingUser.getEmail(),
                        newVerificationCode
                );
                return;
            } catch (MessagingException e) {
                throw new ServiceException("Failed to send verification email: " + e.getMessage());
            }
        }

        // 2. create new user
        User user = User.builder()
                .email(authRequest.email())
                .password(passwordEncoder.encode(authRequest.password()))
                .role(Role.USER)
                .emailVerified(false)
                .createdAt(LocalDateTime.now())
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
        user.setVerifiedAt(LocalDateTime.now());
        userRepository.save(user);

        // 7. Create profile for the user
        Profile profile = Profile.builder()
                .user(user)
                .firstName("")
                .lastName("")
                .avatarUrl("")
                .telephone("")
                .gender(null)
                .bio("")
                .build();
        profileRepository.save(profile);

        // 8. Delete verification record
        emailVerificationCodeRepository.delete(verificationRecord);

    }

    @Override
    public LoginResponse login(AuthRequest authRequest, HttpServletResponse response) {
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
        String jwt = jwtUtil.generateJwtToken(user.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(maxAge);
        response.addCookie(jwtCookie);

        return new LoginResponse(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}
