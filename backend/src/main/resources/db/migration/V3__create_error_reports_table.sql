-- Create error_reports table
CREATE TABLE error_reports (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    topic VARCHAR(255) NOT NULL,
    category VARCHAR(255) NOT NULL,
    priority VARCHAR(255) NOT NULL,
    details TEXT NOT NULL,
    snapshot_file_name VARCHAR(255),
    snapshot_file_path VARCHAR(255),
    game_state_json CLOB,
    status VARCHAR(50) NOT NULL DEFAULT 'NEW',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    user_id VARCHAR(255),
    game_id VARCHAR(255)
);

-- Index voor sneller zoeken
CREATE INDEX idx_error_reports_user_id ON error_reports(user_id);
CREATE INDEX idx_error_reports_game_id ON error_reports(game_id);
CREATE INDEX idx_error_reports_status ON error_reports(status);
CREATE INDEX idx_error_reports_created_at ON error_reports(created_at);
