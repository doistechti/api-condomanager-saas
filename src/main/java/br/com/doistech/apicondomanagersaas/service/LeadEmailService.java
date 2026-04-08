package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.domain.assinatura.Assinatura;
import br.com.doistech.apicondomanagersaas.domain.condominio.Condominio;
import br.com.doistech.apicondomanagersaas.domain.lead.Lead;
import br.com.doistech.apicondomanagersaas.service.email.EmailTemplateService;
import br.com.doistech.apicondomanagersaas.service.email.MailDeliveryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Service
public class LeadEmailService {

    private static final String LEAD_CONFIRMATION_TEMPLATE = "templates/emails/lead-created-confirmation.html";
    private static final String LEAD_INTERNAL_TEMPLATE = "templates/emails/lead-created-internal.html";
    private static final String TRIAL_RELEASED_TEMPLATE = "templates/emails/trial-released.html";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final MailDeliveryService mailDeliveryService;
    private final EmailTemplateService templateService;
    private final String frontendBaseUrl;
    private final Set<String> internalLeadRecipients;

    public LeadEmailService(
            MailDeliveryService mailDeliveryService,
            EmailTemplateService templateService,
            @Value("${app.frontend.base-url:http://localhost:3000}") String frontendBaseUrl,
            @Value("${app.mail.internal.leads-recipients:}") String internalLeadRecipients
    ) {
        this.mailDeliveryService = mailDeliveryService;
        this.templateService = templateService;
        this.frontendBaseUrl = frontendBaseUrl;
        this.internalLeadRecipients = parseRecipients(internalLeadRecipients);
    }

    public void sendLeadCreatedEmails(Lead lead) {
        sendLeadConfirmation(lead);
        sendInternalLeadAlertIfConfigured(lead);
    }

    public void sendTrialReleasedEmail(Lead lead, Condominio condominio, Assinatura assinatura) {
        String subject = condominio.getNome() + " - Trial liberado";
        String html = templateService.render(
                TRIAL_RELEASED_TEMPLATE,
                Map.of(
                        "nomeResponsavel", lead.getResponsavel(),
                        "nomeCondominio", condominio.getNome(),
                        "dataVencimento", assinatura.getDataVencimento().format(DATE_FORMATTER),
                        "portalUrl", templateService.normalizeUrl(frontendBaseUrl)
                ),
                "Template de e-mail de trial liberado não encontrado."
        );

        mailDeliveryService.sendHtml(lead.getEmail(), subject, html, "enviar a liberação do trial");
    }

    private void sendLeadConfirmation(Lead lead) {
        String subject = "Recebemos seu interesse no CondoManager";
        String html = templateService.render(
                LEAD_CONFIRMATION_TEMPLATE,
                Map.of(
                        "nomeResponsavel", lead.getResponsavel(),
                        "nomeCondominio", lead.getNomeCondominio()
                ),
                "Template de e-mail de confirmação do lead não encontrado."
        );

        mailDeliveryService.sendHtml(lead.getEmail(), subject, html, "enviar a confirmação do lead");
    }

    private void sendInternalLeadAlertIfConfigured(Lead lead) {
        if (internalLeadRecipients.isEmpty()) {
            return;
        }

        String subject = "Novo lead recebido - " + lead.getNomeCondominio();
        String html = templateService.render(
                LEAD_INTERNAL_TEMPLATE,
                Map.of(
                        "nomeCondominio", lead.getNomeCondominio(),
                        "responsavel", lead.getResponsavel(),
                        "email", lead.getEmail(),
                        "telefone", nullToEmpty(lead.getTelefone()),
                        "unidadesEstimadas", lead.getUnidadesEstimadas() == null ? "" : String.valueOf(lead.getUnidadesEstimadas()),
                        "mensagem", nullToEmpty(lead.getMensagem())
                ),
                "Template de e-mail interno de lead não encontrado."
        );

        mailDeliveryService.sendHtml(internalLeadRecipients, subject, html, "enviar o alerta interno de lead");
    }

    private Set<String> parseRecipients(String rawRecipients) {
        if (rawRecipients == null || rawRecipients.isBlank()) {
            return Set.of();
        }

        Set<String> recipients = new LinkedHashSet<>();
        Arrays.stream(rawRecipients.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .forEach(recipients::add);
        return recipients;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
