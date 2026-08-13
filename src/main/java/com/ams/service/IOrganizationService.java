package com.ams.service;

import com.ams.dto.OrganizationRequest;
import com.ams.dto.OrganizationResponse;
import com.ams.dto.UpdateOrganizationRequest;

import java.util.List;

public interface IOrganizationService {
    OrganizationResponse createOrganization(String email, OrganizationRequest request);
    List<OrganizationResponse> getMyOrganizations(String email);
    OrganizationResponse getOrganizationById(String email, Long organizationId);
    OrganizationResponse updateOrganization(String email, Long organizationId, UpdateOrganizationRequest request);
}
