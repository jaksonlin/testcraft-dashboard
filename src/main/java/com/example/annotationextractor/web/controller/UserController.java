package com.example.annotationextractor.web.controller;

import com.example.annotationextractor.service.UserManagementService;
import com.example.annotationextractor.web.dto.CreateUserRequestDto;
import com.example.annotationextractor.web.dto.UserSummaryDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class UserController {

    private final UserManagementService userManagementService;

    public UserController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserSummaryDto>> listUsers() {
        return ResponseEntity.ok(userManagementService.listUsers());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequestDto request) {
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            return ResponseEntity.badRequest().body("Username is required");
        }
        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            // Default to basic USER role if none specified
            request.setRoles(List.of("ROLE_USER"));
        }

        try {
            userManagementService.createUserWithDefaultPassword(request);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}


