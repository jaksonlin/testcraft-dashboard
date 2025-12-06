package com.example.annotationextractor.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider,
                                   UserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String jwt = resolveToken(request);
        String requestPath = request.getRequestURI();
        String method = request.getMethod();
        
        // Debug logging for generate-token endpoint
        if (requestPath.contains("/auth/generate-token")) {
            System.out.println("DEBUG JWT Filter: Processing /auth/generate-token");
            System.out.println("DEBUG JWT Filter: Request method = " + method);
            System.out.println("DEBUG JWT Filter: Request path = " + requestPath);
            System.out.println("DEBUG JWT Filter: Authorization header = " + request.getHeader("Authorization"));
            System.out.println("DEBUG JWT Filter: JWT token present = " + (jwt != null));
            if (jwt != null) {
                System.out.println("DEBUG JWT Filter: JWT token length = " + jwt.length());
                System.out.println("DEBUG JWT Filter: JWT token valid = " + tokenProvider.validateToken(jwt));
            }
        }

        if (jwt != null && tokenProvider.validateToken(jwt)) {
            try {
                String username = tokenProvider.getUsername(jwt);
                if (requestPath.contains("/auth/generate-token")) {
                    System.out.println("DEBUG JWT Filter: Username from token = " + username);
                }
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                if (requestPath.contains("/auth/generate-token")) {
                    System.out.println("DEBUG JWT Filter: Authentication set successfully");
                }
            } catch (org.springframework.security.core.userdetails.UsernameNotFoundException ex) {
                // User not found - log but don't set authentication
                // Don't clear context - let Spring Security handle it
                logger.warn("User not found for JWT token: " + ex.getMessage());
                if (requestPath.contains("/auth/generate-token")) {
                    System.out.println("DEBUG JWT Filter: UsernameNotFoundException: " + ex.getMessage());
                }
            } catch (Exception ex) {
                // Other exceptions - log but don't clear context
                // Let Spring Security handle authentication failure
                logger.error("Failed to set authentication from JWT token: " + ex.getMessage(), ex);
                if (requestPath.contains("/auth/generate-token")) {
                    System.out.println("DEBUG JWT Filter: Exception: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        } else {
            if (requestPath.contains("/auth/generate-token")) {
                System.out.println("DEBUG JWT Filter: No valid JWT token found");
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}


