package br.com.doistech.apicondomanagersaas.domain.chat;

import br.com.doistech.apicondomanagersaas.domain.condominio.Condominio;
import br.com.doistech.apicondomanagersaas.domain.pessoaUnidade.PessoaUnidade;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversas",
        indexes = {
                @Index(name = "idx_conversas_condominio", columnList = "condominio_id"),
                @Index(name = "idx_conversas_tipo_status", columnList = "tipo,status"),
                @Index(name = "idx_conversas_ultima_msg", columnList = "ultima_mensagem_at")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "condominio_id", nullable = false)
    private Condominio condominio;

    // Para suporte_condominio (1:1 morador <-> admin_condominio)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "morador_id")
    private PessoaUnidade moradorVinculo; // PessoaUnidade (ehMorador=true) já existe

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversaTipo tipo;

    @Column
    private String titulo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversaStatus status;

    @Column(name = "ultima_mensagem_at")
    private LocalDateTime ultimaMensagemAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}