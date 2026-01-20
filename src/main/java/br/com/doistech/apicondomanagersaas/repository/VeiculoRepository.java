package br.com.doistech.apicondomanagersaas.repository;

import br.com.doistech.apicondomanagersaas.domain.veiculo.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {
    List<Veiculo> findAllByCondominioId(Long condominioId);
    List<Veiculo> findAllByPessoaIdAndCondominioId(Long pessoaId, Long condominioId);
    Optional<Veiculo> findByIdAndCondominioId(Long id, Long condominioId);
}
