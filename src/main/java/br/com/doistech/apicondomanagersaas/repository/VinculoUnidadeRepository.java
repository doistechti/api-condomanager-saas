package br.com.doistech.apicondomanagersaas.repository;

import br.com.doistech.apicondomanagersaas.domain.vinculo.VinculoUnidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VinculoUnidadeRepository extends JpaRepository<VinculoUnidade, Long> {
    List<VinculoUnidade> findAllByCondominioId(Long condominioId);
    List<VinculoUnidade> findAllByUnidadeIdAndCondominioId(Long unidadeId, Long condominioId);
    Optional<VinculoUnidade> findByIdAndCondominioId(Long id, Long condominioId);

    default Optional<VinculoUnidade> findByCondominioIdAndPessoaIdAndUnidadeId(
            Long condominioId,
            Long pessoaId,
            Long unidadeId
    ) {
        return findAllByCondominioIdAndPessoaIdAndUnidadeIdOrderByDataFimAscUpdatedAtDescCreatedAtDescIdDesc(
                condominioId,
                pessoaId,
                unidadeId
        ).stream().findFirst();
    }

    List<VinculoUnidade> findAllByCondominioIdAndPessoaIdAndUnidadeIdOrderByDataFimAscUpdatedAtDescCreatedAtDescIdDesc(
            Long condominioId,
            Long pessoaId,
            Long unidadeId
    );
}
