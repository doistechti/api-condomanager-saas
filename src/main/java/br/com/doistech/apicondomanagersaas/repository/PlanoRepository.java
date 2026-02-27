package br.com.doistech.apicondomanagersaas.repository;

import br.com.doistech.apicondomanagersaas.domain.plano.Plano;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlanoRepository extends JpaRepository<Plano, Long> {
    boolean existsByNome(String nome);

    Optional<Plano> findByNome(String nome);
}

