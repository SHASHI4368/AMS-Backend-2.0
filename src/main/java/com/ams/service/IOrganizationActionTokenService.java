package com.ams.service;

import com.ams.entity.Membership;
import com.ams.enums.OrganizationAction;

public interface IOrganizationActionTokenService {
    String generateToken(Membership membership, OrganizationAction action);
    String hashToken(String token);
}
