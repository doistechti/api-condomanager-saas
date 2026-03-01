package br.com.doistech.apicondomanagersaas.domain.chat;

import br.com.doistech.apicondomanagersaas.domain.usuario.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mensagens",
        indexes = {
                @Index(name = "idx_mensagens_conversa", columnList = "conversa_id"),
                @Index(name = "idx_mensagens_lida", columnList = "lida")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mensagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "conversa_id", nullable = false)
    private Conversa conversa;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "remetente_id", nullable = false)
    private Usuario remetente;

    @Enumerated(EnumType.STRING)
    @Column(name = "remetente_tipo", nullable = false)
    private RemetenteTipo remetenteTipo;

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String conteudo;

    @Column(nullable = false)
    private Boolean lida;

    private LocalDateTime createdAt;
}