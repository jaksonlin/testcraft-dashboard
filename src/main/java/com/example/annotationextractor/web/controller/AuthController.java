package com.example.annotationextractor.web.controller;

import com.example.annotationextractor.security.JwtTokenProvider;
import com.example.annotationextractor.web.dto.LoginRequestDto;
import com.example.annotationextractor.web.dto.LoginResponseDto;
import com.example.annotationextractor.web.dto.ChangePasswordRequestDto;
import com.example.annotationextractor.web.dto.TokenRefreshRequestDto;
import com.example.annotationextractor.web.dto.TokenRefreshResponseDto;
import com.example.annotationextractor.web.dto.TokenGenerationResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final long tokenValidityMs;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider tokenProvider,
                          JdbcTemplate jdbcTemplate,
                          PasswordEncoder passwordEncoder,
                          UserDetailsService userDetailsService,
                          @org.springframework.beans.factory.annotation.Value("${testcraft.security.jwt.validity-ms:86400000}") long tokenValidityMs) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
        this.tokenValidityMs = tokenValidityMs;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(request.getUsername());
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        boolean defaultPasswordInUse = false;
        try {
            Boolean flag = jdbcTemplate.queryForObject(
                    "SELECT default_password_in_use FROM users WHERE username = ?",
                    new Object[]{request.getUsername()},
                    Boolean.class
            );
            defaultPasswordInUse = (flag != null && flag);
        } catch (Exception ignored) {
            // If lookup fails, fall back to false – do not block login
        }

        LoginResponseDto response = new LoginResponseDto(token, refreshToken, request.getUsername(), roles, defaultPasswordInUse);
        return ResponseEntity.ok(response);
    }

    /**
     * Refresh an access token using a refresh token.
     * Returns a new access token and a new refresh token.
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody TokenRefreshRequestDto request) {
        if (request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
            return ResponseEntity.badRequest().body("Refresh token is required");
        }

        if (!tokenProvider.validateRefreshToken(request.getRefreshToken())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired refresh token");
        }

        try {
            String username = tokenProvider.getUsername(request.getRefreshToken());
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            // Generate new access token
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
            String newToken = tokenProvider.generateToken(authentication);
            
            // Generate new refresh token
            String newRefreshToken = tokenProvider.generateRefreshToken(username);

            TokenRefreshResponseDto response = new TokenRefreshResponseDto(newToken, newRefreshToken, username);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to refresh token: " + ex.getMessage());
        }
    }

    /**
     * Change the current authenticated user's password.
     * Requires the correct current password and a non-empty new password.
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequestDto request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Not authenticated");
        }

        String username = authentication.getName();

        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            return ResponseEntity.badRequest().body("New password must not be empty");
        }

        try {
            String currentHash = jdbcTemplate.queryForObject(
                    "SELECT password_hash FROM users WHERE username = ?",
                    new Object[]{username},
                    String.class
            );

            if (currentHash == null || !passwordEncoder.matches(request.getCurrentPassword(), currentHash)) {
                return ResponseEntity.badRequest().body("Current password is incorrect");
            }

            String newHash = passwordEncoder.encode(request.getNewPassword());
            jdbcTemplate.update(
                    "UPDATE users SET password_hash = ?, default_password_in_use = FALSE WHERE username = ?",
                    newHash,
                    username
            );

            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Failed to change password");
        }
    }

    /**
     * Generate a new access token and refresh token for the current authenticated user.
     * Useful for MCP clients and API integrations.
     */
    @PostMapping("/generate-token")
    public ResponseEntity<?> generateToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Debug logging
        System.out.println("DEBUG: generateToken called");
        System.out.println("DEBUG: authentication = " + authentication);
        System.out.println("DEBUG: authentication != null = " + (authentication != null));
        if (authentication != null) {
            System.out.println("DEBUG: isAuthenticated = " + authentication.isAuthenticated());
            System.out.println("DEBUG: principal = " + authentication.getPrincipal());
            System.out.println("DEBUG: principal class = " + (authentication.getPrincipal() != null ? authentication.getPrincipal().getClass().getName() : "null"));
        }
        
        // Check if user is authenticated (not anonymous)
        if (authentication == null || !authentication.isAuthenticated()) {
            System.out.println("DEBUG: Authentication check failed - returning 401");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\":\"Unauthorized\",\"message\":\"Not authenticated\"}");
        }
        
        // Check if authentication principal is a UserDetails (real user) not anonymous
        Object principal = authentication.getPrincipal();
        if (principal instanceof String && "anonymousUser".equals(principal)) {
            System.out.println("DEBUG: Principal is anonymousUser - returning 401");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\":\"Unauthorized\",\"message\":\"Invalid authentication\"}");
        }

        try {
            String username = authentication.getName();
            
            // Validate username is not anonymous
            if (username == null || username.isEmpty() || "anonymousUser".equals(username)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"error\":\"Unauthorized\",\"message\":\"Invalid authentication\"}");
            }
            
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            // Generate new access token
            Authentication authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
            String token = tokenProvider.generateToken(authToken);
            
            // Generate new refresh token
            String refreshToken = tokenProvider.generateRefreshToken(username);

            TokenGenerationResponseDto response = new TokenGenerationResponseDto(
                    token, refreshToken, username, tokenValidityMs);
            return ResponseEntity.ok(response);
        } catch (UsernameNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"error\":\"UserNotFound\",\"message\":\"User not found: " + ex.getMessage() + "\"}");
        } catch (Exception ex) {
            // Log the exception for debugging
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"InternalError\",\"message\":\"Failed to generate token: " + ex.getMessage() + "\"}");
        }
    }
}


