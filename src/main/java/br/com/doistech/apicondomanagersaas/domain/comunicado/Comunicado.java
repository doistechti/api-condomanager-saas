package br.com.doistech.apicondomanagersaas.domain.comunicado;

import br.com.doistech.apicondomanagersaas.domain.condominio.Condominio;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "comunicados")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comunicado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "condominio_id")
    private Condominio condominio;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "LONGTEXT")
    private String conteudo;

    @Column(name = "imagem_url", length = 2000)
    private String imagemUrl;

    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false)
    private Boolean ativo;

    // ✅ ADICIONAR (porque a tabela tem NOT NULL)
    @Column(nullable = false)
    private Boolean destaque;

    @Column(name = "data_publicacao", nullable = false)
    private LocalDateTime dataPublicacao;

    @Column(name = "data_expiracao")
    private LocalDateTime dataExpiracao;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}