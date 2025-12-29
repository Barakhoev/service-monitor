-- Users
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);

-- Monitored applications
CREATE TABLE IF NOT EXISTS monitored_app (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    health_url VARCHAR(500) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Health checks (time series)
CREATE TABLE IF NOT EXISTS health_check (
    id BIGSERIAL PRIMARY KEY,
    app_id BIGINT NOT NULL REFERENCES monitored_app(id) ON DELETE CASCADE,
    checked_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL,
    http_status INTEGER NULL,
    error_type VARCHAR(30) NOT NULL DEFAULT 'NONE',
    latency_ms INTEGER NULL,
    raw_body VARCHAR(4096) NULL,
    error_message VARCHAR(1000) NULL
);

CREATE INDEX IF NOT EXISTS idx_health_check_app_time ON health_check(app_id, checked_at);

-- Seed users (username/password in README)
INSERT INTO users (username, password_hash, role, enabled)
VALUES
  ('admin', '$2b$10$ahZ8UcxwQZyIvGZI2wOUlO0gHhDJLPRJ2dIkKb.DyTdQRCdVE4qEq', 'ADMIN', TRUE),
  ('user',  '$2b$10$rZ1qo6GwpW2wzJ6JQIgAP.uFhhOdw8vYsTjO2j2v7kf5OMtWhyXty',  'USER',  TRUE)
ON CONFLICT (username) DO NOTHING;
