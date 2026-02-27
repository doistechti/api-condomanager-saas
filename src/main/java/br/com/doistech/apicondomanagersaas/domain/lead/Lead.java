package br.com.doistech.apicondomanagersaas.domain.lead;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "leads",
        indexes = {
                @Index(name = "idx_leads_email", columnList = "email"),
                @Index(name = "idx_leads_status", columnList = "status"),
                @Index(name = "idx_leads_created_at", columnList = "created_at")
        })
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_condominio", nullable = false, length = 180)
    private String nomeCondominio;

    @Column(length = 20)
    private String cnpj;

    @Column(nullable = false, length = 160)
    private String responsavel;

    @Column(nullable = false, length = 180)
    private String email;

    @Column(length = 30)
    private String telefone;

    @Column(name = "unidades_estimadas")
    private Integer unidadesEstimadas;

    @Column(columnDefinition = "TEXT")
    private String mensagem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LeadStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        var now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;

        if (this.status == null) {
            this.status = LeadStatus.NOVO;
        }
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();
    }
}