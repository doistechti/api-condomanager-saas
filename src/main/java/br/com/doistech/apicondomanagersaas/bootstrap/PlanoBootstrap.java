package br.com.doistech.apicondomanagersaas.bootstrap;

import br.com.doistech.apicondomanagersaas.domain.plano.Plano;
import br.com.doistech.apicondomanagersaas.repository.PlanoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Order(4)
@Component
@RequiredArgsConstructor
public class PlanoBootstrap implements ApplicationRunner {

    private final PlanoRepository planoRepository;

    @Override
    public void run(ApplicationArguments args) {
        // Idempotente: cria só se não existir pelo nome
        createIfNotExists(
                "Essencial",
                new BigDecimal("49.90"),
                30,
                2,
                "Para condomínios pequenos começarem com o básico.",
                List.of(
                        "Cadastro de moradores e proprietários",
                        "Cadastro de unidades",
                        "Comunicados",
                        "Reservas de espaços"
                ),
                false,
                true
        );

        createIfNotExists(
                "Profissional",
                new BigDecimal("99.90"),
                120,
                5,
                "Para condomínios médios com recursos completos.",
                List.of(
                        "Tudo do Essencial",
                        "Gestão de veículos",
                        "Documentos do condomínio",
                        "Links úteis",
                        "Chat/Conversas internas"
                ),
                true,  // destaque (popular)
                true
        );

        createIfNotExists(
                "Premium",
                new BigDecimal("149.90"),
                999,   // ou um número alto para “ilimitado” de unidades (se quiser)
                -1,    // ilimitado de admins no banco (se seu mapper usa -1)
                "Para condomínios grandes com capacidade máxima.",
                List.of(
                        "Tudo do Profissional",
                        "Admins ilimitados",
                        "Mais capacidade de unidades",
                        "Prioridade no suporte"
                ),
                false,
                true
        );
    }

    private void createIfNotExists(
            String nome,
            BigDecimal preco,
            int maxUnidades,
            int maxAdmins,
            String descricao,
            List<String> recursos,
            boolean destaque,
            boolean ativo
    ) {
        if (planoRepository.existsByNome(nome)) {
            return;
        }

        Plano plano = Plano.builder()
                .nome(nome)
                .preco(preco)
                .maxUnidades(maxUnidades)
                .maxAdmins(maxAdmins)
                .descricao(descricao)
                .recursos(recursos)
                .destaque(destaque)
                .ativo(ativo)
                .build();

        planoRepository.save(plano);
    }
}

