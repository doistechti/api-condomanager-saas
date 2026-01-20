package br.com.doistech.apicondomanagersaas.domain.espaco;

import br.com.doistech.apicondomanagersaas.domain.condominio.Condominio;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "espacos", indexes = {
        @Index(name = "idx_espacos_condominio", columnList = "condominio_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Espaco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "condominio_id")
    private Condominio condominio;

    @Column(nullable = false)
    private String nome;

    private String descricao;

    /**
     * Regras do espaço (texto). No futuro pode virar rich text.
     */
    @Lob
    private String regras;

    private Integer capacidade;

    private boolean necessitaAprovacao;

    private boolean ativo;

    /**
     * Ex: HORARIO, DIARIA (mantido como String no MVP)
     */
    private String tipoReserva;

    /**
     * Em minutos ou dias (defina sua convenção). Mantido simples.
     */
    private Integer prazoAntecedencia;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
