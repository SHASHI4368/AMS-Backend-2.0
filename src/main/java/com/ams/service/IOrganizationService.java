package com.ams.service;

import com.ams.dto.PageResponse;
import com.ams.dto.organization.OrganizationListResponse;
import com.ams.dto.organization.OrganizationRequest;
import com.ams.dto.organization.OrganizationResponse;
import com.ams.dto.organization.UpdateOrganizationRequest;

import java.util.List;

public interface IOrganizationService {
    OrganizationResponse createOrganization(String email, OrganizationRequest request);
    List<OrganizationResponse> getMyOrganizations(String email);
    OrganizationResponse getOrganizationById(String email, Long organizationId);
    OrganizationResponse updateOrganization(String email, Long organizationId, UpdateOrganizationRequest request);
    PageResponse<OrganizationListResponse> getAllOrganizations(String email, int page, int size, String name);
}
