package br.com.doistech.apicondomanagersaas.repository;

import br.com.doistech.apicondomanagersaas.domain.condominio.CondominioAdminInvite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CondominioAdminInviteRepository extends JpaRepository<CondominioAdminInvite, Long> {
    Optional<CondominioAdminInvite> findByToken(String token);

    Optional<CondominioAdminInvite> findTopByCondominioIdAndAtivoTrueOrderByCreatedAtDesc(Long condominioId);

    Optional<CondominioAdminInvite> findTopByCondominioIdAndAtivoTrueAndAceitoEmIsNullOrderByCreatedAtDesc(Long condominioId);

    Optional<CondominioAdminInvite> findTopByCondominioIdAndAtivoTrueAndAceitoEmIsNullAndExpiraEmAfterOrderByCreatedAtDesc(
            Long condominioId,
            java.time.LocalDateTime expiraEm
    );
}
