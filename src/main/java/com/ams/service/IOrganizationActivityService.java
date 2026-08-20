package com.ams.service;

import com.ams.dto.PageResponse;
import com.ams.dto.organization.OrganizationActivityResponse;
import com.ams.entity.Organization;
import com.ams.entity.OrganizationActivity;
import com.ams.entity.User;
import com.ams.enums.OrganizationActivityType;

public interface IOrganizationActivityService {

    void logActivity(
            User actor,
            Organization organization,
            String description,
            OrganizationActivityType activityType
    );
    PageResponse<OrganizationActivityResponse> getOrganizationActivity(
            String email,
            Long organizationId,
            int page,
            int size
    );
}
