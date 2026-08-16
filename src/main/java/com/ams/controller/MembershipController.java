package com.ams.controller;

import com.ams.dto.ApiResponse;
import com.ams.dto.membership.MembershipRequestResponse;
import com.ams.dto.organization.JoinRequest;
import com.ams.enums.OrganizationAction;
import com.ams.service.MembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.base-path}/memberships")
@RequiredArgsConstructor
public class MembershipController {
    private final MembershipService membershipService;

    @GetMapping("/{organizationId}/pending-requests")
    public ResponseEntity<ApiResponse<List<MembershipRequestResponse>>> getPendingRequests(
            @PathVariable
            Long organizationId,
            Authentication authentication
    ) {
        List<MembershipRequestResponse> membershipRequestResponseList = membershipService.getPendingRequests(
                authentication.getName(),
                organizationId
        );
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Pending requests fetched successfully",
                        membershipRequestResponseList
                )
        );
    }

    @PostMapping("/{organizationId}/join")
    public ResponseEntity<ApiResponse<Void>> requestToJoin(
            @PathVariable
            Long organizationId,
            @RequestBody
            JoinRequest request,
            Authentication authentication
    ) {

        membershipService.requestToJoinOrganization(
                authentication.getName(),
                organizationId,
                request.note()
        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Join request sent successfully",
                        null
                )
        );
    }

    @PostMapping("/{membershipId}/accept")
    public ResponseEntity<ApiResponse<Void>> acceptRequest(
            @PathVariable Long membershipId,
            Authentication authentication
    ) {

        membershipService.respondToJoinRequest(
                authentication.getName(),
                membershipId,
                OrganizationAction.ACCEPT
        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Membership request accepted successfully",
                        null
                )
        );
    }

    @PostMapping("/{membershipId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectRequest(
            @PathVariable Long membershipId,
            Authentication authentication
    ) {

        membershipService.respondToJoinRequest(
                authentication.getName(),
                membershipId,
                OrganizationAction.REJECT
        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Membership request rejected successfully",
                        null
                )
        );
    }
}
