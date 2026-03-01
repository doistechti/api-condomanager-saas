package br.com.doistech.apicondomanagersaas.repository;

import br.com.doistech.apicondomanagersaas.domain.linkutil.LinkUtil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LinkUtilRepository extends JpaRepository<LinkUtil, Long> {

    List<LinkUtil> findAllByCondominioIdOrderByOrdemAsc(Long condominioId);

    Optional<LinkUtil> findByIdAndCondominioId(Long id, Long condominioId);
}