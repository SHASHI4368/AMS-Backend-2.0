package com.ams.controller;

import com.ams.dto.ApiResponse;
import com.ams.dto.PageResponse;
import com.ams.dto.user.UserResponse;
import com.ams.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.base-path}/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/{organizationId}/available")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAvailableUsers(
            @PathVariable Long organizationId,
            @RequestParam(required = false, defaultValue = "")
            String search,
            @RequestParam(defaultValue = "0")
            int page,
            @RequestParam(defaultValue = "20")
            int size,
            Authentication authentication
    ) {

        PageResponse<UserResponse> users =
                userService.getUsersNotInOrganization(
                        authentication.getName(),
                        organizationId,
                        search,
                        page,
                        size
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Available users retrieved successfully",
                        users
                )
        );
    }
}
