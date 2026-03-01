package br.com.doistech.apicondomanagersaas.repository;

import br.com.doistech.apicondomanagersaas.domain.pessoaUnidade.PessoaUnidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PessoaUnidadeRepository extends JpaRepository<PessoaUnidade, Long> {

    List<PessoaUnidade> findAllByCondominioIdAndAtivoTrue(Long condominioId);

    List<PessoaUnidade> findAllByCondominioIdAndUnidadeIdAndAtivoTrue(Long condominioId, Long unidadeId);

    // ✅ novos (fachadas)
    List<PessoaUnidade> findAllByCondominioIdAndEhMoradorTrueAndAtivoTrue(Long condominioId);

    List<PessoaUnidade> findAllByCondominioIdAndEhProprietarioTrueAndAtivoTrue(Long condominioId);

    Optional<PessoaUnidade> findByIdAndCondominioId(Long id, Long condominioId);

    boolean existsByCondominioIdAndUnidadeIdAndPrincipalTrueAndAtivoTrue(Long condominioId, Long unidadeId);

    boolean existsByCondominioIdAndUnidadeIdAndPrincipalTrueAndAtivoTrueAndIdNot(Long condominioId, Long unidadeId, Long id);

    long countByCondominioIdAndEhMoradorTrueAndAtivoTrue(Long condominioId);

    // ✅ Portal MORADOR: vínculos do morador resolvidos pelo usuario_id (JWT)
    List<PessoaUnidade> findAllByUsuarioIdAndEhMoradorTrueAndAtivoTrue(Long usuarioId);
}

//package br.com.doistech.apicondomanagersaas.repository;
//
//import br.com.doistech.apicondomanagersaas.domain.pessoaUnidade.PessoaUnidade;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.List;
//import java.util.Optional;
//
//public interface PessoaUnidadeRepository extends JpaRepository<PessoaUnidade, Long> {
//
//    List<PessoaUnidade> findAllByCondominioIdAndAtivoTrue(Long condominioId);
//
//    List<PessoaUnidade> findAllByCondominioIdAndUnidadeIdAndAtivoTrue(Long condominioId, Long unidadeId);
//
//    // ✅ novos (fachadas)
//    List<PessoaUnidade> findAllByCondominioIdAndEhMoradorTrueAndAtivoTrue(Long condominioId);
//
//    List<PessoaUnidade> findAllByCondominioIdAndEhProprietarioTrueAndAtivoTrue(Long condominioId);
//
//    Optional<PessoaUnidade> findByIdAndCondominioId(Long id, Long condominioId);
//
//    boolean existsByCondominioIdAndUnidadeIdAndPrincipalTrueAndAtivoTrue(Long condominioId, Long unidadeId);
//
//    boolean existsByCondominioIdAndUnidadeIdAndPrincipalTrueAndAtivoTrueAndIdNot(Long condominioId, Long unidadeId, Long id);
//
//    long countByCondominioIdAndEhMoradorTrueAndAtivoTrue(Long condominioId);
//}