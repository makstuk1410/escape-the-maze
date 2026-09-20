CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE game_results (
    id UUID PRIMARY KEY,
    game_id UUID NOT NULL UNIQUE,
    user_id UUID NOT NULL REFERENCES users(id),
    difficulty VARCHAR(32) NOT NULL CHECK (difficulty IN ('EASY', 'NORMAL', 'HARD', 'EXPERT')),
    generator VARCHAR(32) NOT NULL,
    score INTEGER NOT NULL CHECK (score >= 0),
    status VARCHAR(32) NOT NULL CHECK (status IN ('WON', 'LOST', 'TIMED_OUT')),
    started_at TIMESTAMPTZ NOT NULL,
    ended_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX game_results_user_id_ended_at_idx
    ON game_results (user_id, ended_at DESC);

CREATE INDEX game_results_difficulty_score_ended_at_idx
    ON game_results (difficulty, score DESC, ended_at ASC);
