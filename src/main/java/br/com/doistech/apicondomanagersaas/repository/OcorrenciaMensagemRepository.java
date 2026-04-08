package br.com.doistech.apicondomanagersaas.repository;

import br.com.doistech.apicondomanagersaas.domain.ocorrencia.OcorrenciaMensagem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OcorrenciaMensagemRepository extends JpaRepository<OcorrenciaMensagem, Long> {

    List<OcorrenciaMensagem> findAllByOcorrenciaIdOrderByCreatedAtAsc(Long ocorrenciaId);

    long countByOcorrenciaId(Long ocorrenciaId);
}
