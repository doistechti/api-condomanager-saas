package br.com.doistech.apicondomanagersaas.domain.pessoa;

import br.com.doistech.apicondomanagersaas.domain.condominio.Condominio;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "pessoas", indexes = {
        @Index(name = "idx_pessoas_condominio", columnList = "condominio_id"),
        @Index(name = "idx_pessoas_email", columnList = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pessoa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "condominio_id")
    private Condominio condominio;

    @Column(nullable = false)
    private String nome;

    /**
     * CPF ou CNPJ (mantido como String; você pode normalizar depois).
     */
    private String documento;

    private String email;
    private String telefone;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
