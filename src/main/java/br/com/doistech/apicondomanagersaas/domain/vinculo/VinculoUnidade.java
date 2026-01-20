package br.com.doistech.apicondomanagersaas.domain.vinculo;

import br.com.doistech.apicondomanagersaas.domain.condominio.Condominio;
import br.com.doistech.apicondomanagersaas.domain.pessoa.Pessoa;
import br.com.doistech.apicondomanagersaas.domain.unidade.Unidade;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "vinculos_unidade", indexes = {
        @Index(name = "idx_vinculos_condominio", columnList = "condominio_id"),
        @Index(name = "idx_vinculos_unidade", columnList = "unidade_id"),
        @Index(name = "idx_vinculos_pessoa", columnList = "pessoa_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VinculoUnidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "condominio_id")
    private Condominio condominio;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id")
    private Unidade unidade;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa_id")
    private Pessoa pessoa;

    private boolean isProprietario;
    private boolean isMorador;

    @Enumerated(EnumType.STRING)
    private TipoMoradia tipoMoradia;

    private LocalDate dataInicio;
    private LocalDate dataFim;

    // Campos opcionais de convite (caso use onboarding)
    private String conviteToken;
    private LocalDateTime conviteEnviadoEm;
    private LocalDateTime conviteAceitoEm;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
