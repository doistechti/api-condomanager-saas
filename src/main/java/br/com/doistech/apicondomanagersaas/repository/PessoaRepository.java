package br.com.doistech.apicondomanagersaas.repository;

import br.com.doistech.apicondomanagersaas.domain.pessoa.Pessoa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PessoaRepository extends JpaRepository<Pessoa, Long> {
    List<Pessoa> findAllByCondominioId(Long condominioId);
    Optional<Pessoa> findByIdAndCondominioId(Long id, Long condominioId);
    Optional<Pessoa> findByCondominioIdAndEmail(Long condominioId, String email);
    Optional<Pessoa> findByCpfCnpj(String cpfCnpj);
}
