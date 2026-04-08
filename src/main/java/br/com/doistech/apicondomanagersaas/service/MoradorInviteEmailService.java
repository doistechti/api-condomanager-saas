package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.domain.pessoaUnidade.PessoaUnidade;
import br.com.doistech.apicondomanagersaas.service.email.EmailTemplateService;
import br.com.doistech.apicondomanagersaas.service.email.MailDeliveryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class MoradorInviteEmailService {
    private static final String TEMPLATE_PATH = "templates/emails/morador-invite.html";

    private final MailDeliveryService mailDeliveryService;
    private final EmailTemplateService templateService;
    private final String frontendBaseUrl;
    private final String mobileAppUrl;

    public MoradorInviteEmailService(
            MailDeliveryService mailDeliveryService,
            EmailTemplateService templateService,
            @Value("${app.frontend.base-url:http://localhost:3000}") String frontendBaseUrl,
            @Value("${app.frontend.mobile-url:https://condoapp.doistech.com.br}") String mobileAppUrl
    ) {
        this.mailDeliveryService = mailDeliveryService;
        this.templateService = templateService;
        this.frontendBaseUrl = frontendBaseUrl;
        this.mobileAppUrl = mobileAppUrl;
    }

    public void sendInvite(PessoaUnidade pessoaUnidade, String senhaTemporaria) {
        String email = pessoaUnidade.getPessoa().getEmail();
        String subject = buildSubject(pessoaUnidade);

        mailDeliveryService.sendHtml(email, subject, buildHtmlBody(pessoaUnidade, senhaTemporaria), "enviar convites");
    }

    private String buildSubject(PessoaUnidade pessoaUnidade) {
        String nomeCondominio = pessoaUnidade.getCondominio() != null ? pessoaUnidade.getCondominio().getNome() : "";
        return nomeCondominio + " - Seu primeiro acesso ao Condomínio Tech.";
    }

    private String buildHtmlBody(PessoaUnidade pessoaUnidade, String senhaTemporaria) {
        return templateService.render(
                TEMPLATE_PATH,
                Map.of(
                        "nomeMorador", pessoaUnidade.getPessoa().getNome(),
                        "nomeCondominio", pessoaUnidade.getCondominio().getNome(),
                        "emailMorador", pessoaUnidade.getPessoa().getEmail(),
                        "numeroUnidade", pessoaUnidade.getUnidade().getIdentificacao(),
                        "senhaTemporaria", senhaTemporaria,
                        "portalWebUrl", templateService.normalizeUrl(frontendBaseUrl),
                        "appUrl", templateService.normalizeUrl(mobileAppUrl)
                ),
                "Template de e-mail do convite do morador não encontrado."
        );
    }
}
