package com.ams.security;

import com.ams.entity.Profile;
import com.ams.entity.User;
import com.ams.repository.ProfileRepository;
import com.ams.repository.UserRepository;
import com.ams.enums.Role;
import com.ams.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final JwtUtil jwtUtil;


    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        // 1. Get the authenticated user from the authentication object
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // 2. Extract the email and name attribute from the OAuth2User
        String email = oAuth2User.getAttribute("email");
        String firstName = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");
        String avatarUrl = oAuth2User.getAttribute("picture");

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .role(Role.USER)
                            .emailVerified(true)
                            .createdAt(LocalDateTime.now())
                            .verifiedAt(LocalDateTime.now())
                            .build();

                    userRepository.save(newUser);

                    Profile newProfile = Profile.builder()
                            .user(newUser)
                            .firstName(firstName)
                            .lastName(lastName)
                            .avatarUrl(avatarUrl)
                            .build();
                    profileRepository.save(newProfile);
                    return newUser;
                });
        String jwt = jwtUtil.generateJwtToken(user.getEmail());

        Cookie cookie = new Cookie("jwt", jwt);

        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(3 * 24 * 60 * 60);

        response.addCookie(cookie);

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        response.sendRedirect(
                "http://localhost:3000/my-appointments"
        );

    }
}
