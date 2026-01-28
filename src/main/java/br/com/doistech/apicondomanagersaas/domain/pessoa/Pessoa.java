package br.com.doistech.apicondomanagersaas.domain.pessoa;

import br.com.doistech.apicondomanagersaas.domain.condominio.Condominio;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "pessoas",
        indexes = {
                @Index(name = "idx_pessoas_cpfcnpj", columnList = "cpf_cnpj", unique = true)
        })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Pessoa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    // Unifica cpf (moradores) e cpf_cnpj (proprietarios)
    @Column(name = "cpf_cnpj", unique = true)
    private String cpfCnpj;

    private String email;
    private String telefone;

    @Column(nullable = false)
    private Boolean ativo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "condominio_id", nullable = false)
    private Condominio condominio;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

