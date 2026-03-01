package br.com.doistech.apicondomanagersaas.domain.linkutil;

import br.com.doistech.apicondomanagersaas.domain.condominio.Condominio;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Links Úteis do Condomínio.
 *
 * Observações:
 * - Multi-tenant é garantido pelo endpoint /api/v1/condominios/{condominioId}/... e pelo TenantIsolationFilter.
 * - Ainda assim, todas as queries/regras usam condominioId para garantir isolamento.
 */
@Entity
@Table(name = "links_uteis")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkUtil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "condominio_id", nullable = false)
    private Condominio condominio;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "LONGTEXT")
    private String descricao;

    @Column(nullable = false, length = 1000)
    private String url;

    private String categoria;

    private Integer ordem;

    @Column(nullable = false)
    private Boolean ativo;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}