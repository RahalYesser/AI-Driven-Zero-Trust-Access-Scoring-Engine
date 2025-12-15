package com.zerotrust.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        // Skip JWT validation only for login endpoint
        final String requestPath = request.getServletPath();
        System.out.println("DEBUG JwtFilter: Processing request to: " + requestPath);
        
        if (requestPath.equals("/api/auth/login")) {
            System.out.println("DEBUG JwtFilter: Skipping JWT validation for login endpoint");
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        System.out.println("DEBUG JwtFilter: Authorization header: " + (authHeader != null ? authHeader.substring(0, Math.min(20, authHeader.length())) + "..." : "null"));
        
        final String jwt;
        final String userEmail;

        // Check if Authorization header is present and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("DEBUG JwtFilter: No valid Authorization header found");
            filterChain.doFilter(request, response);
            return;
        }

        // Extract JWT token
        jwt = authHeader.substring(7);
        System.out.println("DEBUG JwtFilter: Extracted JWT token");
        
        try {
            userEmail = jwtService.extractUsername(jwt);
            System.out.println("DEBUG JwtFilter: Extracted email from token: " + userEmail);

            // If user is not already authenticated
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                System.out.println("DEBUG JwtFilter: Loaded user details for: " + userDetails.getUsername());

                // Validate token
                if (jwtService.validateToken(jwt, userDetails)) {
                    System.out.println("DEBUG JwtFilter: Token is valid, setting authentication");
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("DEBUG JwtFilter: Authentication set successfully");
                } else {
                    System.out.println("DEBUG JwtFilter: Token validation failed");
                }
            } else {
                System.out.println("DEBUG JwtFilter: Email is null or user already authenticated");
            }
        } catch (Exception e) {
            // Invalid token - continue without authentication
            System.out.println("DEBUG JwtFilter: Exception during JWT processing: " + e.getMessage());
            logger.error("JWT validation failed: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
