package com.ams.service;

import com.ams.dto.PageResponse;
import com.ams.dto.user.UserResponse;

public interface IUserService {
    PageResponse<UserResponse> getUsersNotInOrganization(
            String email,
            Long organizationId,
            String search,
            int page,
            int size
    );
}
