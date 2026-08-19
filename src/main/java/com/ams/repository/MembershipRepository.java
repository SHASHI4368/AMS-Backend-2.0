package com.ams.repository;

import com.ams.entity.Membership;
import com.ams.entity.Organization;
import com.ams.entity.User;
import com.ams.enums.MembershipStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
    List<Membership> findByUserAndStatus(User user, MembershipStatus status);
    Optional<Membership> findByUserAndOrganization(User user, Organization organization);
    Optional<Membership> findByStatusAndOrganization(MembershipStatus status, Organization organization);
    @Query("""
        SELECT m
        FROM Membership m
        JOIN FETCH m.user u
        LEFT JOIN FETCH u.profile p
        WHERE m.organization.id = :organizationId
          AND m.status = :status
          AND (
                :search IS NULL
                OR :search = ''
                OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(p.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :search, '%'))
          )
        """)
    Page<Membership> findOrganizationMembers(
            @Param("organizationId") Long organizationId,
            @Param("status") MembershipStatus status,
            @Param("search") String search,
            Pageable pageable
    );
}
