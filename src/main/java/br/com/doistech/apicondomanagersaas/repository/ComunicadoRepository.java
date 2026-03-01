package br.com.doistech.apicondomanagersaas.repository;

import br.com.doistech.apicondomanagersaas.domain.comunicado.Comunicado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComunicadoRepository extends JpaRepository<Comunicado, Long> {

    List<Comunicado> findAllByCondominioIdOrderByDataPublicacaoDesc(Long condominioId);

    Optional<Comunicado> findByIdAndCondominioId(Long id, Long condominioId);
}
