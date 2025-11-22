-- V16: User and role tables for authentication / authorization

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    default_password_in_use BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Seed default roles
INSERT INTO roles (name)
SELECT r.name
FROM (VALUES ('ROLE_ADMIN'), ('ROLE_USER')) AS r(name)
LEFT JOIN roles existing ON existing.name = r.name
WHERE existing.id IS NULL;

-- Seed an initial admin user with a simple default password.
-- The password is stored using Spring's delegating encoder {noop} prefix,
-- so it can be authenticated while we still use bcrypt for new users.
INSERT INTO users (username, password_hash, enabled, default_password_in_use)
SELECT 'admin', '{noop}admin123', TRUE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

-- Ensure the admin user has both ADMIN and USER roles
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name IN ('ROLE_ADMIN', 'ROLE_USER')
LEFT JOIN user_roles ur ON ur.user_id = u.id AND ur.role_id = r.id
WHERE u.username = 'admin' AND ur.user_id IS NULL;


