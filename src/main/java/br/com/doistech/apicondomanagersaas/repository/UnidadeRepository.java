package br.com.doistech.apicondomanagersaas.repository;

import br.com.doistech.apicondomanagersaas.domain.unidade.Unidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UnidadeRepository extends JpaRepository<Unidade, Long> {
    List<Unidade> findAllByCondominioId(Long condominioId);
    Optional<Unidade> findByIdAndCondominioId(Long id, Long condominioId);

    // ✅ Portal MORADOR: listar unidades do escopo (ids) dentro do condomínio do usuário
    List<Unidade> findAllByIdInAndCondominioId(List<Long> ids, Long condominioId);

    long countByCondominioIdAndAtivoTrue(Long condominioId);
}