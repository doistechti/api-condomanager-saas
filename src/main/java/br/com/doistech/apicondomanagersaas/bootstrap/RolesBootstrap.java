package br.com.doistech.apicondomanagersaas.bootstrap;

import br.com.doistech.apicondomanagersaas.domain.role.Role;
import br.com.doistech.apicondomanagersaas.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Bootstrap de roles
 * - Roda na inicialização
 * - Cria apenas o que não existir (idempotente)
 * - Ideal para DEV/LOCAL; em produção o melhor é Flyway
 */
@Order(2)
@Component
public class RolesBootstrap implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(RolesBootstrap.class);

    private final RoleRepository roleRepository;

    public RolesBootstrap(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Iniciando bootstrap de roles...");

        List<String> rolesObrigatorias = List.of(
                "ADMIN_SAAS",
                "ADMIN_CONDOMINIO",
                "MORADOR"
        );

        for (String nomeRole : rolesObrigatorias) {
            if (roleRepository.existsByNome(nomeRole)) {
                log.debug("Role '{}' já existe — ignorando criação", nomeRole);
            } else {
                Role role = new Role();
                role.setNome(nomeRole);
                roleRepository.save(role);
                log.info("Role '{}' criada com sucesso", nomeRole);
            }
        }

        log.info("Bootstrap de roles finalizado");
    }
}