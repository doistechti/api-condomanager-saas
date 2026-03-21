CREATE TABLE aws_credentials (
    id BIGINT NOT NULL AUTO_INCREMENT,
    access_key VARCHAR(255) NOT NULL,
    secret_key VARCHAR(500) NOT NULL,
    region VARCHAR(120) NOT NULL,
    bucket VARCHAR(255) NOT NULL,
    endpoint VARCHAR(500) NULL,
    path_style_access TINYINT(1) NOT NULL DEFAULT 0,
    ativo TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NULL,
    updated_at DATETIME NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;
