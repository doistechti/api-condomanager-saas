ALTER TABLE ocorrencias
  ADD COLUMN codigo VARCHAR(40) NULL AFTER unidade_id;

UPDATE ocorrencias
SET codigo = CONCAT('OCR-', LPAD(id, 6, '0'))
WHERE codigo IS NULL;

ALTER TABLE ocorrencias
  MODIFY COLUMN codigo VARCHAR(40) NOT NULL;

ALTER TABLE ocorrencias
  ADD CONSTRAINT uk_ocorrencias_codigo UNIQUE (codigo);
