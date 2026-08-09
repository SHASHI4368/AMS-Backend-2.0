package com.ams.security;

import com.ams.service.MyUserDetailsService;
import com.ams.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final MyUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        // 1. Get the Authorization header from the request
        String authorizationHeader = request.getHeader("Authorization");

        // 2. Check if the header is present and starts with "Bearer "
        if(authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            // 3. Extract the token from the header
            String token = authorizationHeader.substring(7);

            // 4. Extract the email from the token
            String email = jwtUtil.extractEmail(token);

            // 5. Check if the email is not null and the user is not already authenticated
            if(email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 6. Load the user details using the email
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // 7. Validate the token
                if(jwtUtil.validateToken(token, email, userDetails)) {
                    // 8. Create an authentication token
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );

                    // 9. Set the details of the authentication token
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 10. Set the authentication in the security context
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                }
            }
        }
        // 11. Continue the filter chain
        filterChain.doFilter(request, response);
    }
}
