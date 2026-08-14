package com.ams.repository;

import com.ams.entity.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Page<Organization> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
