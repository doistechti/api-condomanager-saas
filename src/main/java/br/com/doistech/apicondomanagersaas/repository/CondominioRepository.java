package br.com.doistech.apicondomanagersaas.repository;

import br.com.doistech.apicondomanagersaas.domain.condominio.Condominio;
import br.com.doistech.apicondomanagersaas.dto.adminsaas.RecentCondominioResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CondominioRepository extends JpaRepository<Condominio, Long> {

    Optional<Condominio> findByNome(String nome);
    boolean existsByNome(String nome);

    @Query("select c from Condominio c left join fetch c.plano")
    List<Condominio> findAllWithPlano();

    @Query("select c from Condominio c left join fetch c.plano where c.id = :id")
    Optional<Condominio> findByIdWithPlano(@Param("id") Long id);

    // ---- Dashboard Admin-SaaS ----
    @Query("""
        select new br.com.doistech.apicondomanagersaas.dto.adminsaas.RecentCondominioResponse(
            c.id, c.nome, c.createdAt, p.nome
        )
        from Condominio c
        left join c.plano p
        order by c.createdAt desc
    """)
    List<RecentCondominioResponse> findRecentCondominios(Pageable pageable);
}