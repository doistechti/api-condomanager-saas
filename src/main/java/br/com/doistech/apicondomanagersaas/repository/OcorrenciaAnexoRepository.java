package br.com.doistech.apicondomanagersaas.repository;

import br.com.doistech.apicondomanagersaas.domain.ocorrencia.OcorrenciaAnexo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OcorrenciaAnexoRepository extends JpaRepository<OcorrenciaAnexo, Long> {

    List<OcorrenciaAnexo> findAllByOcorrenciaIdOrderByCreatedAtAsc(Long ocorrenciaId);

    long countByOcorrenciaId(Long ocorrenciaId);
}
