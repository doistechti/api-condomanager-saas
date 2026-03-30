package br.com.doistech.apicondomanagersaas.domain.usuario;

import br.com.doistech.apicondomanagersaas.domain.role.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false, length = 255)
    private String senha;

    @Builder.Default
    @Column(nullable = false)
    private Boolean ativo = true;

    @Builder.Default
    @Column(name = "primeiro_acesso", nullable = false)
    private Boolean primeiroAcesso = false;

    @Column(name = "primeiro_acesso_concluido_em")
    private LocalDateTime primeiroAcessoConcluidoEm;

    @Column(name = "condominio_id")
    private Long condominioId;

    @Column(name = "reset_senha_token_hash", length = 64)
    private String resetSenhaTokenHash;

    @Column(name = "reset_senha_expira_em")
    private LocalDateTime resetSenhaExpiraEm;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "usuario_roles",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
}
