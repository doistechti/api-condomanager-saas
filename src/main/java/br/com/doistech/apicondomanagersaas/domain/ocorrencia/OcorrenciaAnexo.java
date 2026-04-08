package br.com.doistech.apicondomanagersaas.domain.ocorrencia;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ocorrencia_anexos",
        indexes = {
                @Index(name = "idx_ocorrencia_anexos_ocorrencia", columnList = "ocorrencia_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OcorrenciaAnexo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ocorrencia_id", nullable = false)
    private Ocorrencia ocorrencia;

    @Column(name = "arquivo_url", nullable = false, length = 1024)
    private String arquivoUrl;

    @Column(name = "arquivo_nome", nullable = false, length = 255)
    private String arquivoNome;

    @Column(name = "content_type", length = 120)
    private String contentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_arquivo", nullable = false, length = 40)
    private OcorrenciaArquivoTipo tipoArquivo;

    @Column(name = "tamanho_bytes")
    private Long tamanhoBytes;

    private LocalDateTime createdAt;
}
