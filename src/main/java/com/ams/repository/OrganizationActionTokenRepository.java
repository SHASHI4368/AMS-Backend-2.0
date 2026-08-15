package com.ams.repository;

import com.ams.entity.OrganizationActionToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizationActionTokenRepository extends JpaRepository<OrganizationActionToken,Long> {
    Optional<OrganizationActionToken> findByTokenHash(String tokenHash);
}
