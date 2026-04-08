package br.com.doistech.apicondomanagersaas.domain.pet;

import br.com.doistech.apicondomanagersaas.domain.condominio.Condominio;
import br.com.doistech.apicondomanagersaas.domain.unidade.Unidade;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pets", indexes = {
        @Index(name = "idx_pets_condominio", columnList = "condominio_id"),
        @Index(name = "idx_pets_unidade", columnList = "unidade_id"),
        @Index(name = "idx_pets_nome", columnList = "nome")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "condominio_id")
    private Condominio condominio;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id")
    private Unidade unidade;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String tipo;

    private String raca;
    private String porte;
    private String cor;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Lob
    private String observacoes;

    @Column(name = "foto_url")
    private String fotoUrl;

    @Column(name = "foto_nome")
    private String fotoNome;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
