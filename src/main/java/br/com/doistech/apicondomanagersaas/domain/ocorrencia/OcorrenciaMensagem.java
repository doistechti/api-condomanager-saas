package br.com.doistech.apicondomanagersaas.domain.ocorrencia;

import br.com.doistech.apicondomanagersaas.domain.usuario.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ocorrencia_mensagens",
        indexes = {
                @Index(name = "idx_ocorrencia_mensagens_ocorrencia", columnList = "ocorrencia_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OcorrenciaMensagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ocorrencia_id", nullable = false)
    private Ocorrencia ocorrencia;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    @Enumerated(EnumType.STRING)
    @Column(name = "autor_tipo", nullable = false, length = 40)
    private OcorrenciaAutorTipo autorTipo;

    @Column(name = "mensagem", columnDefinition = "LONGTEXT", nullable = false)
    private String mensagem;

    private LocalDateTime createdAt;
}
