package br.com.doistech.apicondomanagersaas.repository;

import br.com.doistech.apicondomanagersaas.domain.pessoaUnidade.PessoaUnidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PessoaUnidadeRepository extends JpaRepository<PessoaUnidade, Long> {

    List<PessoaUnidade> findAllByCondominioIdAndAtivoTrue(Long condominioId);

    List<PessoaUnidade> findAllByCondominioIdAndUnidadeIdAndAtivoTrue(Long condominioId, Long unidadeId);

    Optional<PessoaUnidade> findByIdAndCondominioId(Long id, Long condominioId);

    // REGRA B: bloquear se já existir outro principal (considerando somente ativos)
    boolean existsByCondominioIdAndUnidadeIdAndPrincipalTrueAndAtivoTrue(Long condominioId, Long unidadeId);

    boolean existsByCondominioIdAndUnidadeIdAndPrincipalTrueAndAtivoTrueAndIdNot(Long condominioId, Long unidadeId, Long id);
}


