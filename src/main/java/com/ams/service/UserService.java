package com.ams.service;

import com.ams.dto.PageResponse;
import com.ams.dto.user.UserResponse;
import com.ams.entity.Organization;
import com.ams.entity.User;
import com.ams.enums.MembershipStatus;
import com.ams.repository.UserRepository;
import com.ams.util.ServiceUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService{
    private final UserRepository userRepository;
    private final ServiceUtil serviceUtil;

    @Override
    @Transactional
    public PageResponse<UserResponse> getUsersNotInOrganization(
            String email,
            Long organizationId,
            String search,
            int page,
            int size
    ) {
        // Check if the user exists
        User user = serviceUtil.getUser(email);

        // Check if the organization exists
        Organization organization =  serviceUtil.getOrganization(organizationId);

        // Check if the user is the owner of the organization
        serviceUtil.isUserOwnerOfOrganization(user,organization);

        // Get users not in the organization
        String normalizedSearch = search == null ? "" : search.trim();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "email"));
        Page<User> usersPage = userRepository.findUsersNotInOrganization(
                organization.getId(),
                List.of(MembershipStatus.ACTIVE, MembershipStatus.PENDING),
                normalizedSearch,
                pageable
        );
        List<User> users = usersPage.getContent();

        // Convert to UserResponse
        List<UserResponse> userResponses = users.stream()
                .map(u -> new UserResponse(
                        u.getId(),
                        u.getEmail(),
                        u.getProfile() != null ? u.getProfile().getFirstName() : null,
                        u.getProfile() != null ? u.getProfile().getLastName() : null,
                        u.getProfile() != null ? u.getProfile().getAvatarUrl() : null
                ))
                .toList();

        // Return paginated response
        return new PageResponse<>(
                userResponses,
                usersPage.getNumber(),
                usersPage.getSize(),
                usersPage.getTotalElements(),
                usersPage.getTotalPages(),
                usersPage.isLast()
        );

    }
}
