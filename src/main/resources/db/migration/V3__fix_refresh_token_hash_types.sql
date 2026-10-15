ALTER TABLE refresh_tokens
    ALTER COLUMN token_hash TYPE VARCHAR(64),
    ALTER COLUMN replaced_by_hash TYPE VARCHAR(64);