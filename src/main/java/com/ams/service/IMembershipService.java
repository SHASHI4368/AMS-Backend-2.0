package com.ams.service;

import com.ams.dto.membership.MembershipRequestResponse;
import com.ams.enums.OrganizationAction;

import java.util.List;

public interface IMembershipService {
    List<MembershipRequestResponse> getPendingRequests(String email, Long organizationId);
    void requestToJoinOrganization(String email, Long organizationId, String note);
    void processEmailJoinRequestAction(String rawToken, OrganizationAction expectedAction);
    void respondToJoinRequest(String ownerEmail, Long membershipId, OrganizationAction action);
}
