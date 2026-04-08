package br.com.doistech.apicondomanagersaas.repository;

import br.com.doistech.apicondomanagersaas.domain.ocorrencia.Ocorrencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OcorrenciaRepository extends JpaRepository<Ocorrencia, Long> {

    List<Ocorrencia> findAllByCondominioIdOrderByUpdatedAtDesc(Long condominioId);

    List<Ocorrencia> findAllByCondominioIdAndMoradorVinculoUsuarioIdOrderByUpdatedAtDesc(Long condominioId, Long usuarioId);

    Optional<Ocorrencia> findByIdAndCondominioId(Long id, Long condominioId);
}
