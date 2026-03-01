package br.com.doistech.apicondomanagersaas.domain.documentosCondominio;

import br.com.doistech.apicondomanagersaas.domain.condominio.Condominio;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "documentos_condominio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentoCondominio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "condominio_id")
    private Condominio condominio;

    @Column(nullable = false)
    private String nome;

    private String descricao;

    @Column(nullable = false, length = 2000)
    private String arquivoUrl;

    @Column(nullable = false, length = 500)
    private String arquivoNome;

    private String categoria;

    @Column(nullable = false)
    private Boolean ativo;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}