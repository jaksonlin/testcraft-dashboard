-- V1: Initial schema for test analytics (PostgreSQL)
-- Tables are created in dependency order

-- Scan sessions: one record per scan execution
CREATE TABLE IF NOT EXISTS scan_sessions (
    id BIGSERIAL PRIMARY KEY,
    scan_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    scan_directory VARCHAR(500) NOT NULL,
    total_repositories INT DEFAULT 0,
    total_test_classes INT DEFAULT 0,
    total_test_methods INT DEFAULT 0,
    total_annotated_methods INT DEFAULT 0,
    scan_duration_ms BIGINT DEFAULT 0,
    scan_status VARCHAR(50) DEFAULT 'COMPLETED',
    error_log TEXT,
    metadata TEXT
);

-- Teams (optional grouping for repositories)
CREATE TABLE IF NOT EXISTS teams (
    id BIGSERIAL PRIMARY KEY,
    team_name VARCHAR(255) NOT NULL,
    team_code VARCHAR(50) NOT NULL UNIQUE,
    department VARCHAR(255),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Repositories being scanned
CREATE TABLE IF NOT EXISTS repositories (
    id BIGSERIAL PRIMARY KEY,
    repository_name VARCHAR(255) NOT NULL,
    repository_path VARCHAR(500) NOT NULL,
    git_url VARCHAR(500),
    git_branch VARCHAR(100) DEFAULT 'main',
    technology_stack TEXT,
    team_id BIGINT,
    first_scan_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_scan_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_test_classes INT DEFAULT 0,
    total_test_methods INT DEFAULT 0,
    total_annotated_methods INT DEFAULT 0,
    annotation_coverage_rate DECIMAL(5,2) DEFAULT 0.00,
    UNIQUE (git_url),
    CONSTRAINT fk_repositories_team FOREIGN KEY (team_id) REFERENCES teams(id)
);

-- Test classes discovered in repositories
CREATE TABLE IF NOT EXISTS test_classes (
    id BIGSERIAL PRIMARY KEY,
    repository_id BIGINT,
    class_name VARCHAR(255) NOT NULL,
    package_name VARCHAR(500),
    file_path VARCHAR(500),
    file_size_bytes BIGINT,
    total_test_methods INT DEFAULT 0,
    annotated_test_methods INT DEFAULT 0,
    coverage_rate DECIMAL(5,2) DEFAULT 0.00,
    first_seen_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    scan_session_id BIGINT,
    UNIQUE (repository_id, file_path),
    CONSTRAINT fk_test_classes_repo FOREIGN KEY (repository_id) REFERENCES repositories(id) ON DELETE CASCADE,
    CONSTRAINT fk_test_classes_scan FOREIGN KEY (scan_session_id) REFERENCES scan_sessions(id)
);

-- Test methods and their annotation metadata
CREATE TABLE IF NOT EXISTS test_methods (
    id BIGSERIAL PRIMARY KEY,
    test_class_id BIGINT,
    method_name VARCHAR(255) NOT NULL,
    method_signature TEXT,
    line_number INT,
    has_annotation BOOLEAN DEFAULT FALSE,
    annotation_data TEXT,
    annotation_title VARCHAR(500),
    annotation_author VARCHAR(255),
    annotation_status VARCHAR(100),
    annotation_target_class VARCHAR(255),
    annotation_target_method VARCHAR(255),
    annotation_description TEXT,
    annotation_tags TEXT,
    annotation_test_points TEXT,
    annotation_requirements TEXT,
    annotation_defects TEXT,
    annotation_testcases TEXT,
    annotation_last_update_time VARCHAR(255),
    annotation_last_update_author VARCHAR(255),
    first_seen_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    scan_session_id BIGINT,
    UNIQUE (test_class_id, method_name, method_signature),
    CONSTRAINT fk_test_methods_class FOREIGN KEY (test_class_id) REFERENCES test_classes(id) ON DELETE CASCADE,
    CONSTRAINT fk_test_methods_scan FOREIGN KEY (scan_session_id) REFERENCES scan_sessions(id)
);

-- Daily aggregated metrics
CREATE TABLE IF NOT EXISTS daily_metrics (
    id BIGSERIAL PRIMARY KEY,
    metric_date DATE NOT NULL,
    total_repositories INT DEFAULT 0,
    total_test_classes INT DEFAULT 0,
    total_test_methods INT DEFAULT 0,
    total_annotated_methods INT DEFAULT 0,
    overall_coverage_rate DECIMAL(5,2) DEFAULT 0.00,
    new_test_methods INT DEFAULT 0,
    new_annotated_methods INT DEFAULT 0,
    UNIQUE (metric_date)
);

-- Basic indexes
CREATE INDEX IF NOT EXISTS idx_repositories_name ON repositories(repository_name);
CREATE INDEX IF NOT EXISTS idx_repositories_team_id ON repositories(team_id);
CREATE INDEX IF NOT EXISTS idx_test_classes_repo ON test_classes(repository_id);
CREATE INDEX IF NOT EXISTS idx_test_methods_class ON test_methods(test_class_id);
CREATE INDEX IF NOT EXISTS idx_scan_sessions_date ON scan_sessions(scan_date);
CREATE INDEX IF NOT EXISTS idx_daily_metrics_date ON daily_metrics(metric_date);
CREATE INDEX IF NOT EXISTS idx_annotation_data ON test_methods(annotation_data);

-- Performance/compound indexes
CREATE INDEX IF NOT EXISTS idx_test_classes_repo_class ON test_classes (repository_id, class_name, package_name);
CREATE INDEX IF NOT EXISTS idx_test_methods_annotation ON test_methods (has_annotation);
CREATE INDEX IF NOT EXISTS idx_repositories_last_scan ON repositories (last_scan_date);
CREATE INDEX IF NOT EXISTS idx_repositories_coverage ON repositories (annotation_coverage_rate DESC);
CREATE INDEX IF NOT EXISTS idx_test_methods_composite ON test_methods (test_class_id, has_annotation, scan_session_id);


