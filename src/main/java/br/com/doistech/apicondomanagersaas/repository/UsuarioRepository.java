package br.com.doistech.apicondomanagersaas.repository;

import br.com.doistech.apicondomanagersaas.domain.usuario.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    interface AdminCondominioAccessProjection {
        Long getId();
        LocalDateTime getPrimeiroAcessoConcluidoEm();
    }

    Optional<Usuario> findByEmail(String email);

    @Query("""
        select u.id as id, u.primeiroAcessoConcluidoEm as primeiroAcessoConcluidoEm
        from Usuario u
        join u.roles r
        where u.condominioId = :condominioId
          and r.nome = 'ADMIN_CONDOMINIO'
        order by u.id asc
    """)
    Optional<AdminCondominioAccessProjection> findFirstAdminCondominioAccessByCondominioId(Long condominioId);

    Optional<Usuario> findByResetSenhaTokenHash(String resetSenhaTokenHash);

    boolean existsByEmail(String email);
}
