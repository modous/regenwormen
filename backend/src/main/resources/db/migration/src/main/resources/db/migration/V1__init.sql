-- Minimale tabellen om Flyway te testen. Later breiden we dit uit.
CREATE TABLE IF NOT EXISTS games (
    id UUID PRIMARY KEY,
    round_number INT NOT NULL DEFAULT 0,
    current_index INT NOT NULL DEFAULT 0,
    is_over BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS players (
    id UUID PRIMARY KEY,
    game_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    points INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_players_game
    FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE
    );
