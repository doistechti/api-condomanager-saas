package br.com.doistech.apicondomanagersaas.domain.ocorrencia;

import br.com.doistech.apicondomanagersaas.domain.condominio.Condominio;
import br.com.doistech.apicondomanagersaas.domain.pessoaUnidade.PessoaUnidade;
import br.com.doistech.apicondomanagersaas.domain.unidade.Unidade;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ocorrencias",
        indexes = {
                @Index(name = "idx_ocorrencias_condominio", columnList = "condominio_id"),
                @Index(name = "idx_ocorrencias_morador", columnList = "morador_id"),
                @Index(name = "idx_ocorrencias_status", columnList = "status"),
                @Index(name = "idx_ocorrencias_categoria", columnList = "categoria")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ocorrencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "condominio_id", nullable = false)
    private Condominio condominio;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "morador_id", nullable = false)
    private PessoaUnidade moradorVinculo;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", nullable = false)
    private Unidade unidade;

    @Column(nullable = false, length = 40, unique = true)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 80)
    private OcorrenciaCategoria categoria;

    @Column(nullable = false, length = 255)
    private String titulo;

    @Column(name = "descricao", columnDefinition = "LONGTEXT", nullable = false)
    private String descricao;

    @Column(name = "local_ocorrencia", length = 255)
    private String localOcorrencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private OcorrenciaStatus status;

    @Column(name = "resolvida_em")
    private LocalDateTime resolvidaEm;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
