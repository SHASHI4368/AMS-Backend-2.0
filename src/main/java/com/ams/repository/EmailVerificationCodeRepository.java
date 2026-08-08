package com.ams.repository;

import com.ams.entity.EmailVerificationCode;
import com.ams.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationCodeRepository extends JpaRepository<EmailVerificationCode, Long> {
    Optional<EmailVerificationCode> findByUser(User user);
    void deleteByUser(User user);
}
