package br.com.doistech.apicondomanagersaas.repository;

import br.com.doistech.apicondomanagersaas.domain.notificacao.PushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {

    Optional<PushSubscription> findByEndpoint(String endpoint);

    List<PushSubscription> findAllByUsuarioIdAndAtivoTrue(Long usuarioId);

    List<PushSubscription> findAllByUsuarioIdInAndAtivoTrue(Collection<Long> usuarioIds);

    @Query("""
        select distinct ps
        from PushSubscription ps
        join ps.usuario u
        join u.roles r
        where u.condominioId = :condominioId
          and u.ativo = true
          and ps.ativo = true
          and r.nome = 'ADMIN_CONDOMINIO'
    """)
    List<PushSubscription> findActiveAdminSubscriptionsByCondominioId(Long condominioId);
}
