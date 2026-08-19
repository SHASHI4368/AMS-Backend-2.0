package com.ams.controller;

import com.ams.dto.ApiResponse;
import com.ams.dto.PageResponse;
import com.ams.dto.membership.MembershipResponse;
import com.ams.dto.organization.*;
import com.ams.service.MembershipService;
import com.ams.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.base-path}/organizations")
@RequiredArgsConstructor
public class OrganizationController {
    private final OrganizationService organizationService;
    private final MembershipService membershipService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<OrganizationResponse>> createOrganization(
            @RequestBody
            @Valid
            OrganizationRequest request,
            Authentication authentication
    ) {
        OrganizationResponse response = organizationService
                .createOrganization(authentication.getName(), request);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Organization created successfully",
                        response
                )
        );
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<OrganizationResponse>>> getMyOrganizations(
            Authentication authentication
    ) {
        List<OrganizationResponse> response = organizationService
                .getMyOrganizations(authentication.getName());
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "My organizations fetched successfully",
                        response
                )
        );
    }

    @GetMapping("/{organizationId}")
    public ResponseEntity<ApiResponse<OrganizationResponse>> getOrganizationById(
            @PathVariable("organizationId") Long organizationId,
            Authentication authentication
    ){
        OrganizationResponse response = organizationService
                .getOrganizationById(authentication.getName(), organizationId);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Organization fetched successfully",
                        response
                )
        );
    }

    @PutMapping("/{organizationId}")
    public ResponseEntity<ApiResponse<OrganizationResponse>> updateOrganization(
            @PathVariable("organizationId") Long organizationId,
            @RequestBody
            @Valid
            UpdateOrganizationRequest request,
            Authentication authentication
    ){
        OrganizationResponse response = organizationService
                .updateOrganization(authentication.getName(), organizationId, request);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Organization updated successfully",
                        response
                )
        );
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<PageResponse<OrganizationListResponse>>> getAllOrganizations(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "name", required = false, defaultValue = "") String name,
            Authentication authentication
    ){
        PageResponse<OrganizationListResponse> response = organizationService
                .getAllOrganizations(authentication.getName(), page, size, name);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Organizations fetched successfully",
                        response
                )
        );
    }

    @GetMapping("/{organizationId}/members")
    public ResponseEntity<ApiResponse<PageResponse<MembershipResponse>>> getMembers(
            @PathVariable
            Long organizationId,
            @RequestParam(required = false)
            String search,
            @RequestParam(value = "page", defaultValue = "0")
            int page,
            @RequestParam(value = "size", defaultValue = "10")
            int size,
            Authentication authentication
    ) {
        PageResponse<MembershipResponse> membershipResponse = membershipService.getOrganizationMembers(
                authentication.getName(),
                organizationId,
                search,
                page,
                size
        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Members fetched successfully",
                        membershipResponse
                )
        );
    }
}
