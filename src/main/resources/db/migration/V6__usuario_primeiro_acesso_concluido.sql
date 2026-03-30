ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS primeiro_acesso TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS primeiro_acesso_concluido_em TIMESTAMP NULL;

UPDATE usuarios
SET primeiro_acesso_concluido_em = COALESCE(primeiro_acesso_concluido_em, CURRENT_TIMESTAMP)
WHERE ativo = 1
  AND primeiro_acesso = 0
  AND primeiro_acesso_concluido_em IS NULL;
