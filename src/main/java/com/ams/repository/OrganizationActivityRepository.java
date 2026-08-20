package com.ams.repository;

import com.ams.entity.Organization;
import com.ams.entity.OrganizationActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationActivityRepository extends JpaRepository<OrganizationActivity, Long> {
    Page<OrganizationActivity> findByOrganization(Organization organization, Pageable pageable);
}
