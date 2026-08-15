package com.ams.service;

import com.ams.entity.Membership;
import com.ams.entity.OrganizationActionToken;
import com.ams.enums.OrganizationAction;
import com.ams.exception.ServiceException;
import com.ams.repository.OrganizationActionTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class OrganizationActionTokenService implements IOrganizationActionTokenService{
    private final OrganizationActionTokenRepository organizationActionTokenRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generateToken(Membership membership, OrganizationAction action) {
        // Generate 256-bit random number and convert it to a hex string
        byte[] randomBytes = new byte[32]; // 256 bits
        secureRandom.nextBytes(randomBytes);

        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        // Save the hash
        String tokenHash = hashToken(rawToken);

        OrganizationActionToken token = OrganizationActionToken.builder()
                .membership(membership)
                .action(action)
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now().plusHours(24)) // Token expires in 24 hours
                .build();

        organizationActionTokenRepository.save(token);

        return rawToken;
    }

    @Override
    public String hashToken(String token) {
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));

            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        }catch (NoSuchAlgorithmException e){
            throw new ServiceException("Error hashing token");
        }
    }
}
