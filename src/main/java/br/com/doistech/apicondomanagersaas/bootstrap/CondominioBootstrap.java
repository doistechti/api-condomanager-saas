package br.com.doistech.apicondomanagersaas.bootstrap;

import br.com.doistech.apicondomanagersaas.domain.condominio.Condominio;
import br.com.doistech.apicondomanagersaas.repository.CondominioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Order(1)
@Component
public class CondominioBootstrap implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CondominioBootstrap.class);

    public static final String CONDOMINIO_TESTE_NOME = "Condomínio Teste";

    private final CondominioRepository condominioRepository;

    public CondominioBootstrap(CondominioRepository condominioRepository) {
        this.condominioRepository = condominioRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Iniciando bootstrap de condomínio...");

        Condominio condominioTeste = condominioRepository.findByNome(CONDOMINIO_TESTE_NOME)
                .orElseGet(() -> {
                    Condominio novo = new Condominio();
                    novo.setNome(CONDOMINIO_TESTE_NOME);
                    Condominio salvo = condominioRepository.save(novo);
                    log.info("Condomínio '{}' criado com sucesso (id={})", salvo.getNome(), salvo.getId());
                    return salvo;
                });

        if (condominioTeste.getId() != null) {
            log.info("Condomínio teste pronto (id={})", condominioTeste.getId());
        }

        log.info("Bootstrap de condomínio finalizado");
    }
}

