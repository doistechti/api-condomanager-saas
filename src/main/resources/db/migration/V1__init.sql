-- MySQL 8+ (InnoDB)

CREATE TABLE condominios (
  id BIGINT NOT NULL AUTO_INCREMENT,
  nome VARCHAR(255) NOT NULL,
  cnpj VARCHAR(50) NULL,
  responsavel VARCHAR(255) NULL,
  email VARCHAR(255) NULL,
  telefone VARCHAR(50) NULL,
  endereco VARCHAR(255) NULL,
  tipo_setor VARCHAR(50) NULL,
  logo_url VARCHAR(500) NULL,
  created_at DATETIME NULL,
  updated_at DATETIME NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE setores (
  id BIGINT NOT NULL AUTO_INCREMENT,
  condominio_id BIGINT NOT NULL,
  nome VARCHAR(255) NOT NULL,
  tipo VARCHAR(50) NULL,
  descricao VARCHAR(255) NULL,
  created_at DATETIME NULL,
  updated_at DATETIME NULL,
  PRIMARY KEY (id),
  KEY idx_setores_condominio (condominio_id),
  CONSTRAINT fk_setores_condominio FOREIGN KEY (condominio_id) REFERENCES condominios(id)
) ENGINE=InnoDB;

CREATE TABLE unidades (
  id BIGINT NOT NULL AUTO_INCREMENT,
  condominio_id BIGINT NOT NULL,
  setor_id BIGINT NULL,
  identificacao VARCHAR(255) NOT NULL,
  descricao VARCHAR(255) NULL,
  created_at DATETIME NULL,
  updated_at DATETIME NULL,
  PRIMARY KEY (id),
  KEY idx_unidades_condominio (condominio_id),
  KEY idx_unidades_setor (setor_id),
  CONSTRAINT fk_unidades_condominio FOREIGN KEY (condominio_id) REFERENCES condominios(id),
  CONSTRAINT fk_unidades_setor FOREIGN KEY (setor_id) REFERENCES setores(id)
) ENGINE=InnoDB;

CREATE TABLE pessoas (
  id BIGINT NOT NULL AUTO_INCREMENT,
  condominio_id BIGINT NOT NULL,
  nome VARCHAR(255) NOT NULL,
  documento VARCHAR(50) NULL,
  email VARCHAR(255) NULL,
  telefone VARCHAR(50) NULL,
  created_at DATETIME NULL,
  updated_at DATETIME NULL,
  PRIMARY KEY (id),
  KEY idx_pessoas_condominio (condominio_id),
  KEY idx_pessoas_email (email),
  CONSTRAINT fk_pessoas_condominio FOREIGN KEY (condominio_id) REFERENCES condominios(id)
) ENGINE=InnoDB;

CREATE TABLE vinculos_unidade (
  id BIGINT NOT NULL AUTO_INCREMENT,
  condominio_id BIGINT NOT NULL,
  unidade_id BIGINT NOT NULL,
  pessoa_id BIGINT NOT NULL,
  is_proprietario BIT(1) NOT NULL DEFAULT b'0',
  is_morador BIT(1) NOT NULL DEFAULT b'0',
  tipo_moradia VARCHAR(30) NULL,
  data_inicio DATE NULL,
  data_fim DATE NULL,
  convite_token VARCHAR(255) NULL,
  convite_enviado_em DATETIME NULL,
  convite_aceito_em DATETIME NULL,
  created_at DATETIME NULL,
  updated_at DATETIME NULL,
  PRIMARY KEY (id),
  KEY idx_vinculos_condominio (condominio_id),
  KEY idx_vinculos_unidade (unidade_id),
  KEY idx_vinculos_pessoa (pessoa_id),
  CONSTRAINT fk_vinculos_condominio FOREIGN KEY (condominio_id) REFERENCES condominios(id),
  CONSTRAINT fk_vinculos_unidade FOREIGN KEY (unidade_id) REFERENCES unidades(id),
  CONSTRAINT fk_vinculos_pessoa FOREIGN KEY (pessoa_id) REFERENCES pessoas(id)
) ENGINE=InnoDB;

CREATE TABLE veiculos (
  id BIGINT NOT NULL AUTO_INCREMENT,
  condominio_id BIGINT NOT NULL,
  pessoa_id BIGINT NOT NULL,
  placa VARCHAR(20) NOT NULL,
  modelo VARCHAR(255) NULL,
  cor VARCHAR(50) NULL,
  tipo VARCHAR(50) NULL,
  created_at DATETIME NULL,
  updated_at DATETIME NULL,
  PRIMARY KEY (id),
  KEY idx_veiculos_condominio (condominio_id),
  KEY idx_veiculos_pessoa (pessoa_id),
  KEY idx_veiculos_placa (placa),
  CONSTRAINT fk_veiculos_condominio FOREIGN KEY (condominio_id) REFERENCES condominios(id),
  CONSTRAINT fk_veiculos_pessoa FOREIGN KEY (pessoa_id) REFERENCES pessoas(id)
) ENGINE=InnoDB;

CREATE TABLE espacos (
  id BIGINT NOT NULL AUTO_INCREMENT,
  condominio_id BIGINT NOT NULL,
  nome VARCHAR(255) NOT NULL,
  descricao VARCHAR(255) NULL,
  regras LONGTEXT NULL,
  capacidade INT NULL,
  necessita_aprovacao BIT(1) NOT NULL DEFAULT b'0',
  ativo BIT(1) NOT NULL DEFAULT b'1',
  tipo_reserva VARCHAR(50) NULL,
  prazo_antecedencia INT NULL,
  created_at DATETIME NULL,
  updated_at DATETIME NULL,
  PRIMARY KEY (id),
  KEY idx_espacos_condominio (condominio_id),
  CONSTRAINT fk_espacos_condominio FOREIGN KEY (condominio_id) REFERENCES condominios(id)
) ENGINE=InnoDB;

CREATE TABLE reservas (
  id BIGINT NOT NULL AUTO_INCREMENT,
  condominio_id BIGINT NOT NULL,
  espaco_id BIGINT NOT NULL,
  vinculo_id BIGINT NOT NULL,
  data_reserva DATE NOT NULL,
  hora_inicio TIME NULL,
  hora_fim TIME NULL,
  status VARCHAR(20) NOT NULL,
  motivo_recusa VARCHAR(255) NULL,
  observacoes LONGTEXT NULL,
  created_at DATETIME NULL,
  updated_at DATETIME NULL,
  PRIMARY KEY (id),
  KEY idx_reservas_condominio (condominio_id),
  KEY idx_reservas_espaco (espaco_id),
  KEY idx_reservas_data (data_reserva),
  CONSTRAINT fk_reservas_condominio FOREIGN KEY (condominio_id) REFERENCES condominios(id),
  CONSTRAINT fk_reservas_espaco FOREIGN KEY (espaco_id) REFERENCES espacos(id),
  CONSTRAINT fk_reservas_vinculo FOREIGN KEY (vinculo_id) REFERENCES vinculos_unidade(id)
) ENGINE=InnoDB;
