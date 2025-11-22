package com.example.annotationextractor.web.controller;

import com.example.annotationextractor.security.JwtTokenProvider;
import com.example.annotationextractor.web.dto.LoginRequestDto;
import com.example.annotationextractor.web.dto.LoginResponseDto;
import com.example.annotationextractor.web.dto.ChangePasswordRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider tokenProvider,
                          JdbcTemplate jdbcTemplate,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
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

        LoginResponseDto response = new LoginResponseDto(token, request.getUsername(), roles, defaultPasswordInUse);
        return ResponseEntity.ok(response);
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
}


