CREATE TABLE user_mappings (
    internal_user_id BIGSERIAL PRIMARY KEY,
    clerk_user_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_user_mappings_clerk_user UNIQUE (clerk_user_id)
);

CREATE INDEX idx_user_mappings_clerk_user ON user_mappings(clerk_user_id);
