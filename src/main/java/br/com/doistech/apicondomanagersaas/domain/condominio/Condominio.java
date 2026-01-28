package br.com.doistech.apicondomanagersaas.domain.condominio;

import br.com.doistech.apicondomanagersaas.domain.plano.Plano;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "condominios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Condominio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    private String cnpj;

    private String responsavel;

    private String email;

    private String telefone;

    private String endereco;

    /**
     * Define se o condomínio é dividido por "blocos", "quadras", etc.
     */
    private String tipoSetor;

    private String logoUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plano_id")
    private Plano plano;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
