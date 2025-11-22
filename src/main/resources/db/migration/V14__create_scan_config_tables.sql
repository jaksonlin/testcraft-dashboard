CREATE TABLE IF NOT EXISTS scan_settings (
    id BIGSERIAL PRIMARY KEY,
    repository_hub_path TEXT NOT NULL,
    temp_clone_mode BOOLEAN NOT NULL DEFAULT FALSE,
    max_repositories_per_scan INTEGER NOT NULL DEFAULT 100,
    scheduler_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    daily_scan_cron TEXT NOT NULL DEFAULT '0 0 2 * * ?',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO scan_settings (id, repository_hub_path, temp_clone_mode, max_repositories_per_scan, scheduler_enabled, daily_scan_cron)
VALUES (1, './repositories', FALSE, 100, TRUE, '0 0 2 * * ?')
ON CONFLICT (id) DO NOTHING;

CREATE TABLE IF NOT EXISTS scan_repository_configs (
    id BIGSERIAL PRIMARY KEY,
    repository_url TEXT NOT NULL UNIQUE,
    team_name TEXT NOT NULL,
    team_code TEXT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

