package com.ams.controller;

import com.ams.dto.ApiResponse;
import com.ams.dto.PageResponse;
import com.ams.dto.organization.OrganizationActivityResponse;
import com.ams.service.OrganizationActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.base-path}/organization-activities")
@RequiredArgsConstructor
public class OrganizationActivityController {
    private final OrganizationActivityService organizationActivityService;

    @GetMapping("/{organizationId}")
    public ResponseEntity<ApiResponse<PageResponse<OrganizationActivityResponse>>> getOrganizationActivities(
            @PathVariable
            Long organizationId,
            @RequestParam(value = "page", defaultValue = "0")
            Integer page,
            @RequestParam(value = "size", defaultValue = "10")
            Integer size,
            Authentication authentication
    ) {
        PageResponse<OrganizationActivityResponse> response = organizationActivityService
                .getOrganizationActivity(authentication.getName(), organizationId, page, size);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Organization activities fetched successfully",
                        response
                )
        );
    }

}
