package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.domain.condominio.CondominioAdminInvite;
import br.com.doistech.apicondomanagersaas.service.email.EmailTemplateService;
import br.com.doistech.apicondomanagersaas.service.email.MailDeliveryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class CondominioAdminInviteEmailService {
    private static final String TEMPLATE_PATH = "templates/emails/condominio-admin-invite.html";

    private final MailDeliveryService mailDeliveryService;
    private final EmailTemplateService templateService;
    private final String frontendBaseUrl;
    private final String invitePath;

    public CondominioAdminInviteEmailService(
            MailDeliveryService mailDeliveryService,
            EmailTemplateService templateService,
            @Value("${app.frontend.base-url:http://localhost:3000}") String frontendBaseUrl,
            @Value("${app.frontend.admin-condominio-convite-path:/convite/admin-condominio}") String invitePath
    ) {
        this.mailDeliveryService = mailDeliveryService;
        this.templateService = templateService;
        this.frontendBaseUrl = frontendBaseUrl;
        this.invitePath = invitePath;
    }

    public void sendInvite(CondominioAdminInvite invite) {
        mailDeliveryService.sendHtml(
                invite.getEmail(),
                invite.getCondominio().getNome() + " - Ative seu acesso administrativo",
                buildHtmlBody(invite, buildInviteUrl(invite.getToken())),
                "enviar convites"
        );
    }

    private String buildInviteUrl(String token) {
        String baseUrl = frontendBaseUrl.endsWith("/") ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1) : frontendBaseUrl;
        String path = invitePath.startsWith("/") ? invitePath : "/" + invitePath;
        return baseUrl + path + "?token=" + token;
    }

    private String buildHtmlBody(CondominioAdminInvite invite, String inviteUrl) {
        return templateService.render(
                TEMPLATE_PATH,
                Map.of(
                        "nomeResponsavel", invite.getNome(),
                        "nomeCondominio", invite.getCondominio().getNome(),
                        "linkConvite", inviteUrl
                ),
                "Template de e-mail do administrador do condomínio não encontrado."
        );
    }
}
