-- =========================
-- AUTH TABLES
-- =========================

CREATE TABLE IF NOT EXISTS roles (
                                     id BIGINT NOT NULL AUTO_INCREMENT,
                                     nome VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_roles_nome (nome)
    );

CREATE TABLE IF NOT EXISTS usuarios (
                                        id BIGINT NOT NULL AUTO_INCREMENT,
                                        nome VARCHAR(120) NOT NULL,
    email VARCHAR(120) NOT NULL,
    senha VARCHAR(255) NOT NULL,
    ativo TINYINT(1) NOT NULL DEFAULT 1,
    condominio_id BIGINT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_usuarios_email (email),
    KEY idx_usuarios_condominio (condominio_id),
    CONSTRAINT fk_usuarios_condominio
    FOREIGN KEY (condominio_id) REFERENCES condominios(id)
    );

CREATE TABLE IF NOT EXISTS usuario_roles (
                                             usuario_id BIGINT NOT NULL,
                                             role_id BIGINT NOT NULL,
                                             PRIMARY KEY (usuario_id, role_id),
    CONSTRAINT fk_usuario_roles_usuario
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_usuario_roles_role
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
    );

-- =========================
-- SEED ROLES
-- =========================
INSERT INTO roles (nome) VALUES ('ADMIN_SAAS')
    ON DUPLICATE KEY UPDATE nome = VALUES(nome);

INSERT INTO roles (nome) VALUES ('ADMIN_CONDOMINIO')
    ON DUPLICATE KEY UPDATE nome = VALUES(nome);

INSERT INTO roles (nome) VALUES ('MORADOR')
    ON DUPLICATE KEY UPDATE nome = VALUES(nome);

-- =========================
-- SEED ADMIN (SaaS)
-- senha: Admin@123
-- BCrypt hash gerado:
-- $2b$10$ipQAFrgxcUpaHnCXkzpEnOWS44Eyc/loKXv5.IPPr3kgL7I1.KEgW
-- =========================

INSERT INTO usuarios (nome, email, senha, ativo, condominio_id)
VALUES ('Admin SaaS', 'adminsaas@teste.com', '$2b$10$ipQAFrgxcUpaHnCXkzpEnOWS44Eyc/loKXv5.IPPr3kgL7I1.KEgW', 1, NULL)
    ON DUPLICATE KEY UPDATE
                         nome = VALUES(nome),
                         senha = VALUES(senha),
                         ativo = VALUES(ativo),
                         condominio_id = VALUES(condominio_id);

-- Vincula ADMIN_SAAS ao admin
INSERT INTO usuario_roles (usuario_id, role_id)
SELECT u.id, r.id
FROM usuarios u
         JOIN roles r ON r.nome = 'ADMIN_SAAS'
WHERE u.email = 'adminsaas@teste.com'
    ON DUPLICATE KEY UPDATE
                         usuario_id = usuario_id,
                         role_id = role_id;
