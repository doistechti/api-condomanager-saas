package br.com.doistech.apicondomanagersaas.domain.pessoaUnidade;

import br.com.doistech.apicondomanagersaas.domain.condominio.Condominio;
import br.com.doistech.apicondomanagersaas.domain.pessoa.Pessoa;
import br.com.doistech.apicondomanagersaas.domain.unidade.Unidade;
import br.com.doistech.apicondomanagersaas.domain.usuario.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pessoas_unidades",
        indexes = {
                @Index(name = "idx_pu_cond_unid", columnList = "condominio_id,unidade_id"),
                @Index(name = "idx_pu_unid_pessoa", columnList = "unidade_id,pessoa_id", unique = true)
        })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class PessoaUnidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Multi-tenant explícito no vínculo
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "condominio_id", nullable = false)
    private Condominio condominio;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", nullable = false)
    private Unidade unidade;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa_id", nullable = false)
    private Pessoa pessoa;

    // Papéis no vínculo
    @Column(nullable = false)
    private Boolean ehProprietario = false;

    @Column(nullable = false)
    private Boolean ehMorador = false;

    /**
     * ✅ NOVO: tipo do morador (quando ehMorador=true)
     * (para não perder o dado que hoje existe no Supabase)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "morador_tipo")
    private MoradorTipo moradorTipo;

    // Pessoa de referência da unidade
    @Column(nullable = false)
    private Boolean principal = false;

    private LocalDate dataInicio;
    private LocalDate dataFim;

    // Login opcional
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    // Convite
    private String conviteToken;
    private LocalDateTime conviteEnviadoEm;
    private LocalDateTime conviteAceitoEm;

    @Column(nullable = false)
    private Boolean ativo = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}