package br.com.doistech.apicondomanagersaas.repository;

import br.com.doistech.apicondomanagersaas.domain.espaco.Espaco;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EspacoRepository extends JpaRepository<Espaco, Long> {
    List<Espaco> findAllByCondominioId(Long condominioId);
    Optional<Espaco> findByIdAndCondominioId(Long id, Long condominioId);
}
