package com.ams.repository;

import com.ams.entity.Membership;
import com.ams.entity.Organization;
import com.ams.entity.User;
import com.ams.enums.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
    List<Membership> findByUserAndStatus(User user, MembershipStatus status);
    Optional<Membership> findByUserAndOrganization(User user, Organization organization);
}
