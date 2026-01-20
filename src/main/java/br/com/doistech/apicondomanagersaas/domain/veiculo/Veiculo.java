package br.com.doistech.apicondomanagersaas.domain.veiculo;

import br.com.doistech.apicondomanagersaas.domain.condominio.Condominio;
import br.com.doistech.apicondomanagersaas.domain.pessoa.Pessoa;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "veiculos", indexes = {
        @Index(name = "idx_veiculos_condominio", columnList = "condominio_id"),
        @Index(name = "idx_veiculos_pessoa", columnList = "pessoa_id"),
        @Index(name = "idx_veiculos_placa", columnList = "placa")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Veiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "condominio_id")
    private Condominio condominio;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa_id")
    private Pessoa pessoa;

    @Column(nullable = false)
    private String placa;

    private String modelo;
    private String cor;

    /**
     * Carro, Moto, etc (mantido como String no MVP).
     */
    private String tipo;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
