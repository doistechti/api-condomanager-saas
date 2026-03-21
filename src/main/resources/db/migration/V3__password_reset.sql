ALTER TABLE usuarios
    ADD COLUMN reset_senha_token_hash VARCHAR(64) NULL AFTER condominio_id,
    ADD COLUMN reset_senha_expira_em TIMESTAMP NULL AFTER reset_senha_token_hash;

CREATE INDEX idx_usuarios_reset_senha_token_hash
    ON usuarios (reset_senha_token_hash);
