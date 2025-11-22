package com.example.annotationextractor.security;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Loads users from the database for Spring Security.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final JdbcTemplate jdbcTemplate;

    public CustomUserDetailsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            DbUser dbUser = jdbcTemplate.queryForObject(
                    "SELECT id, username, password_hash, enabled FROM users WHERE username = ?",
                    new Object[]{username},
                    new DbUserRowMapper()
            );

            if (dbUser == null) {
                throw new UsernameNotFoundException("User not found: " + username);
            }

            List<String> roleNames = jdbcTemplate.queryForList(
                    "SELECT r.name FROM roles r " +
                            "JOIN user_roles ur ON ur.role_id = r.id " +
                            "WHERE ur.user_id = ?",
                    new Object[]{dbUser.id()},
                    String.class
            );

            List<GrantedAuthority> authorities = roleNames.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            return User.withUsername(dbUser.username())
                    .password(dbUser.passwordHash())
                    .authorities(authorities)
                    .accountLocked(!dbUser.enabled())
                    .disabled(!dbUser.enabled())
                    .build();
        } catch (EmptyResultDataAccessException ex) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
    }

    private record DbUser(Long id, String username, String passwordHash, boolean enabled) {
    }

    private static class DbUserRowMapper implements RowMapper<DbUser> {
        @Override
        public DbUser mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new DbUser(
                    rs.getLong("id"),
                    rs.getString("username"),
                    rs.getString("password_hash"),
                    rs.getBoolean("enabled")
            );
        }
    }
}


