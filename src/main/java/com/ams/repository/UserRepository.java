package com.ams.repository;

import com.ams.entity.User;
import com.ams.enums.MembershipStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("""
    SELECT u
    FROM User u
    LEFT JOIN FETCH u.profile p
    WHERE NOT EXISTS (
        SELECT 1
        FROM Membership m
        WHERE m.user = u
          AND m.organization.id = :organizationId
          AND m.status IN :excludedStatuses
    )
    AND (
        :search IS NULL
        OR :search = ''
        OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(p.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :search, '%'))
    )
""")
    Page<User> findUsersNotInOrganization(
            @Param("organizationId") Long organizationId,
            @Param("excludedStatuses") Collection<MembershipStatus> excludedStatuses,
            @Param("search") String search,
            Pageable pageable
    );
}
