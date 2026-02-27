package br.com.doistech.apicondomanagersaas.domain.unidade;

import br.com.doistech.apicondomanagersaas.domain.condominio.Condominio;
import br.com.doistech.apicondomanagersaas.domain.setor.Setor;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "unidades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Unidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "condominio_id")
    private Condominio condominio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setor_id")
    private Setor setor;

    /**
     * Ex: 101, 202, Casa 12...
     */
    @Column(nullable = false)
    private String identificacao;

    private String descricao;

    /**
     * ✅ IMPORTANTE:
     * Como a entidade usa Lombok @Builder, o valor "ativo = true" NÃO é aplicado no builder
     * sem o @Builder.Default. Sem isso, o builder cria "ativo = null" e o banco quebra (NOT NULL).
     */
    @Builder.Default
    @Column(nullable = false)
    private Boolean ativo = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * ✅ Garantia extra (simples e segura):
     * Mesmo que alguém crie a entidade manualmente e esqueça de setar ativo/timestamps,
     * o JPA garante valores válidos antes de persistir.
     */
    @PrePersist
    public void prePersist() {
        if (ativo == null) ativo = true;
        if (createdAt == null) createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        if (ativo == null) ativo = true;
        updatedAt = LocalDateTime.now();
    }
}