package br.com.doistech.apicondomanagersaas.repository;

import br.com.doistech.apicondomanagersaas.domain.setor.Setor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SetorRepository extends JpaRepository<Setor, Long> {
    List<Setor> findAllByCondominioId(Long condominioId);
    Optional<Setor> findByIdAndCondominioId(Long id, Long condominioId);
}
