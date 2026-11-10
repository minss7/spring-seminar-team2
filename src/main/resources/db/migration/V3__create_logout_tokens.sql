CREATE TABLE logout_tokens (
                               jti VARCHAR(36) NOT NULL PRIMARY KEY,
                               expires_at TIMESTAMP NOT NULL
);