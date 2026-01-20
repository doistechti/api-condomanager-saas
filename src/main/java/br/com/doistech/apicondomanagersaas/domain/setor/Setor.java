package br.com.doistech.apicondomanagersaas.domain.setor;

import br.com.doistech.apicondomanagersaas.domain.condominio.Condominio;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "setores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Setor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "condominio_id")
    private Condominio condominio;

    @Column(nullable = false)
    private String nome;

    /**
     * Ex: BLOCO ou QUADRA (mantido simples como String no MVP).
     */
    private String tipo;

    private String descricao;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
