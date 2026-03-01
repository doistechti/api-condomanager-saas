package br.com.doistech.apicondomanagersaas.repository;

import br.com.doistech.apicondomanagersaas.domain.documentosCondominio.DocumentoCondominio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentoCondominioRepository extends JpaRepository<DocumentoCondominio, Long> {

    List<DocumentoCondominio> findAllByCondominioIdOrderByCreatedAtDesc(Long condominioId);

    Optional<DocumentoCondominio> findByIdAndCondominioId(Long id, Long condominioId);
}
