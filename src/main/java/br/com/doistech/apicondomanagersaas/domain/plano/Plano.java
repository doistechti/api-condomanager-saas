package br.com.doistech.apicondomanagersaas.domain.plano;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "planos")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Plano {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String nome;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(name = "max_unidades", nullable = false)
    private Integer maxUnidades;

    /**
     * Regras:
     * -1  = ilimitado (persistência)
     * 999 = ilimitado (frontend)
     */
    @Column(name = "max_admins", nullable = false)
    private Integer maxAdmins;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    /**
     * Armazenar JSON no banco e trabalhar como List<String> no Java.
     * Se o campo no banco for TEXT, ainda funciona.
     */
    @Convert(converter = RecursosJsonConverter.class)
    private List<String> recursos;

    @Column(nullable = false)
    private Boolean destaque;

    @Column(nullable = false)
    private Boolean ativo;

    @CreationTimestamp
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;
}

