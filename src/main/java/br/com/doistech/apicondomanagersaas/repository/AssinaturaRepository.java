package br.com.doistech.apicondomanagersaas.repository;

import br.com.doistech.apicondomanagersaas.domain.assinatura.Assinatura;
import br.com.doistech.apicondomanagersaas.domain.assinatura.AssinaturaStatus;
import br.com.doistech.apicondomanagersaas.dto.adminsaas.RecentPaymentRow;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AssinaturaRepository extends JpaRepository<Assinatura, Long> {

    boolean existsByCondominioIdAndStatus(Long condominioId, AssinaturaStatus status);

    Optional<Assinatura> findTopByCondominioIdOrderByDataVencimentoDesc(Long condominioId);

    Optional<Assinatura> findTopByCondominioIdAndStatusOrderByDataVencimentoDesc(Long condominioId, AssinaturaStatus status);

    // ---- Dashboard Admin-SaaS ----
    long countByStatus(AssinaturaStatus status);

    @Query("""
        select coalesce(sum(p.preco), 0)
        from Assinatura a
        join a.plano p
        where a.status = :status
    """)
    BigDecimal sumPrecoByStatus(@Param("status") AssinaturaStatus status);

    @Query("""
        select new br.com.doistech.apicondomanagersaas.dto.adminsaas.RecentPaymentRow(
            a.id, c.nome, p.preco, a.status, a.updatedAt
        )
        from Assinatura a
        join a.condominio c
        join a.plano p
        order by a.updatedAt desc
    """)
    List<RecentPaymentRow> findRecentPayments(Pageable pageable);
}