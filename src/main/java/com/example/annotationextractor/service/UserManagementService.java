package com.example.annotationextractor.service;

import com.example.annotationextractor.web.dto.CreateUserRequestDto;
import com.example.annotationextractor.web.dto.UserSummaryDto;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserManagementService {

    private static final String DEFAULT_INITIAL_PASSWORD = "ChangeMe123!";

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    public UserManagementService(JdbcTemplate jdbcTemplate,
                                 PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserSummaryDto> listUsers() {
        String sql = """
                SELECT u.id,
                       u.username,
                       u.enabled,
                       u.default_password_in_use,
                       r.name AS role_name
                FROM users u
                LEFT JOIN user_roles ur ON ur.user_id = u.id
                LEFT JOIN roles r ON r.id = ur.role_id
                ORDER BY u.username
                """;

        List<UserRow> rows = jdbcTemplate.query(sql, new UserRowMapper());

        Map<Long, UserSummaryDto> byId = new HashMap<>();
        for (UserRow row : rows) {
            UserSummaryDto dto = byId.computeIfAbsent(row.id, id -> {
                UserSummaryDto u = new UserSummaryDto();
                u.setId(row.id);
                u.setUsername(row.username);
                u.setEnabled(row.enabled);
                u.setDefaultPasswordInUse(row.defaultPasswordInUse);
                u.setRoles(new ArrayList<>());
                return u;
            });
            if (row.roleName != null && !dto.getRoles().contains(row.roleName)) {
                dto.getRoles().add(row.roleName);
            }
        }

        return new ArrayList<>(byId.values());
    }

    @Transactional
    public void createUserWithDefaultPassword(CreateUserRequestDto request) {
        String encodedPassword = passwordEncoder.encode(DEFAULT_INITIAL_PASSWORD);

        try {
            Long userId = jdbcTemplate.queryForObject(
                    "INSERT INTO users (username, password_hash, enabled, default_password_in_use) " +
                            "VALUES (?, ?, TRUE, TRUE) RETURNING id",
                    new Object[]{request.getUsername(), encodedPassword},
                    Long.class
            );

            if (request.getRoles() != null && !request.getRoles().isEmpty()) {
                for (String roleName : request.getRoles()) {
                    Long roleId = jdbcTemplate.queryForObject(
                            "SELECT id FROM roles WHERE name = ?",
                            new Object[]{roleName},
                            Long.class
                    );
                    if (roleId != null) {
                        jdbcTemplate.update(
                                "INSERT INTO user_roles (user_id, role_id) VALUES (?, ?) " +
                                        "ON CONFLICT (user_id, role_id) DO NOTHING",
                                userId, roleId
                        );
                    }
                }
            }
        } catch (DuplicateKeyException ex) {
            throw new IllegalArgumentException("Username already exists");
        }
    }

    private static class UserRow {
        final Long id;
        final String username;
        final boolean enabled;
        final boolean defaultPasswordInUse;
        final String roleName;

        private UserRow(Long id,
                        String username,
                        boolean enabled,
                        boolean defaultPasswordInUse,
                        String roleName) {
            this.id = id;
            this.username = username;
            this.enabled = enabled;
            this.defaultPasswordInUse = defaultPasswordInUse;
            this.roleName = roleName;
        }
    }

    private static class UserRowMapper implements RowMapper<UserRow> {
        @Override
        public UserRow mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new UserRow(
                    rs.getLong("id"),
                    rs.getString("username"),
                    rs.getBoolean("enabled"),
                    rs.getBoolean("default_password_in_use"),
                    rs.getString("role_name")
            );
        }
    }
}


