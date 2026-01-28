package br.com.doistech.apicondomanagersaas.bootstrap;

import br.com.doistech.apicondomanagersaas.domain.role.Role;
import br.com.doistech.apicondomanagersaas.domain.usuario.Usuario;
import br.com.doistech.apicondomanagersaas.repository.RoleRepository;
import br.com.doistech.apicondomanagersaas.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Order(2)
@Component
public class AdminUsersBootstrap implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminUsersBootstrap.class);

    private final UsuarioRepository usuarioRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUsersBootstrap(
            UsuarioRepository usuarioRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {

        log.info("Iniciando bootstrap de usuários administradores...");

        criarAdminSaas();
        criarAdminCondominio();

        log.info("Bootstrap de usuários administradores finalizado");
    }

    private void criarAdminSaas() {

        String email = "brenno.agostini@gmail.com";

        if (usuarioRepository.existsByEmail(email)) {
            log.info("Admin SaaS '{}' já existe — ignorando criação", email);
            return;
        }

        Role roleAdminSaas = roleRepository.findByNome("ADMIN_SAAS")
                .orElseThrow(() -> new IllegalStateException("Role ADMIN_SAAS não encontrada"));

        Usuario usuario = new Usuario();
        usuario.setNome("Admin SaaS");
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode("37Sele8t@Br"));
        usuario.setAtivo(true);
        usuario.setCondominioId(null); // Admin de plataforma
        usuario.setRoles(Set.of(roleAdminSaas));

        usuarioRepository.save(usuario);

        log.info("Admin SaaS '{}' criado com sucesso", email);
    }

    private void criarAdminCondominio() {

        String email = "condominio@teste.com";

        if (usuarioRepository.existsByEmail(email)) {
            log.info("Admin Condomínio '{}' já existe — ignorando criação", email);
            return;
        }

        Role roleAdminCondominio = roleRepository.findByNome("ADMIN_CONDOMINIO")
                .orElseThrow(() -> new IllegalStateException("Role ADMIN_CONDOMINIO não encontrada"));

        Usuario usuario = new Usuario();
        usuario.setNome("Admin Condomínio Teste");
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode("teste234"));
        usuario.setAtivo(true);
        usuario.setCondominioId(1L); // ⚠️ ID do condomínio teste
        usuario.setRoles(Set.of(roleAdminCondominio));

        usuarioRepository.save(usuario);

        log.info("Admin Condomínio '{}' criado com sucesso", email);
    }
}

