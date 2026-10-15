
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL,

    token_hash CHAR(64) NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    expires_at TIMESTAMP NOT NULL,

    revoked_at TIMESTAMP,

    replaced_by_hash CHAR(64),

    CONSTRAINT uk_refresh_tokens_token_hash
        UNIQUE (token_hash),

    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_refresh_tokens_user_id
    ON refresh_tokens (user_id);

CREATE INDEX idx_refresh_tokens_expires_at
    ON refresh_tokens (expires_at);
