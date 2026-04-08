CREATE TABLE ocorrencias (
  id BIGINT NOT NULL AUTO_INCREMENT,
  condominio_id BIGINT NOT NULL,
  morador_id BIGINT NOT NULL,
  unidade_id BIGINT NOT NULL,
  categoria VARCHAR(80) NOT NULL,
  titulo VARCHAR(255) NOT NULL,
  descricao LONGTEXT NOT NULL,
  local_ocorrencia VARCHAR(255) NULL,
  status VARCHAR(40) NOT NULL,
  resolvida_em DATETIME NULL,
  created_at DATETIME NULL,
  updated_at DATETIME NULL,
  PRIMARY KEY (id),
  KEY idx_ocorrencias_condominio (condominio_id),
  KEY idx_ocorrencias_morador (morador_id),
  KEY idx_ocorrencias_status (status),
  KEY idx_ocorrencias_categoria (categoria),
  CONSTRAINT fk_ocorrencias_condominio FOREIGN KEY (condominio_id) REFERENCES condominios(id),
  CONSTRAINT fk_ocorrencias_morador FOREIGN KEY (morador_id) REFERENCES pessoas_unidades(id),
  CONSTRAINT fk_ocorrencias_unidade FOREIGN KEY (unidade_id) REFERENCES unidades(id)
) ENGINE=InnoDB;

CREATE TABLE ocorrencia_anexos (
  id BIGINT NOT NULL AUTO_INCREMENT,
  ocorrencia_id BIGINT NOT NULL,
  arquivo_url VARCHAR(1024) NOT NULL,
  arquivo_nome VARCHAR(255) NOT NULL,
  content_type VARCHAR(120) NULL,
  tipo_arquivo VARCHAR(40) NOT NULL,
  tamanho_bytes BIGINT NULL,
  created_at DATETIME NULL,
  PRIMARY KEY (id),
  KEY idx_ocorrencia_anexos_ocorrencia (ocorrencia_id),
  CONSTRAINT fk_ocorrencia_anexos_ocorrencia FOREIGN KEY (ocorrencia_id) REFERENCES ocorrencias(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE ocorrencia_mensagens (
  id BIGINT NOT NULL AUTO_INCREMENT,
  ocorrencia_id BIGINT NOT NULL,
  autor_id BIGINT NOT NULL,
  autor_tipo VARCHAR(40) NOT NULL,
  mensagem LONGTEXT NOT NULL,
  created_at DATETIME NULL,
  PRIMARY KEY (id),
  KEY idx_ocorrencia_mensagens_ocorrencia (ocorrencia_id),
  CONSTRAINT fk_ocorrencia_mensagens_ocorrencia FOREIGN KEY (ocorrencia_id) REFERENCES ocorrencias(id) ON DELETE CASCADE,
  CONSTRAINT fk_ocorrencia_mensagens_autor FOREIGN KEY (autor_id) REFERENCES usuarios(id)
) ENGINE=InnoDB;
