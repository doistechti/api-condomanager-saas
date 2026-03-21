CREATE TABLE condominio_admin_invites (
    id BIGINT NOT NULL AUTO_INCREMENT,
    condominio_id BIGINT NOT NULL,
    usuario_id BIGINT NULL,
    nome VARCHAR(120) NOT NULL,
    email VARCHAR(120) NOT NULL,
    token VARCHAR(120) NOT NULL,
    enviado_em DATETIME NULL,
    aceito_em DATETIME NULL,
    expira_em DATETIME NOT NULL,
    ativo TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NULL,
    updated_at DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_condominio_admin_invites_token (token),
    KEY idx_cai_condominio (condominio_id),
    CONSTRAINT fk_cai_condominio FOREIGN KEY (condominio_id) REFERENCES condominios(id),
    CONSTRAINT fk_cai_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
) ENGINE=InnoDB;
