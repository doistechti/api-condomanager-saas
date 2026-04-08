package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.domain.assinatura.Assinatura;
import br.com.doistech.apicondomanagersaas.domain.assinatura.AssinaturaStatus;
import br.com.doistech.apicondomanagersaas.repository.UsuarioRepository;
import br.com.doistech.apicondomanagersaas.service.email.EmailTemplateService;
import br.com.doistech.apicondomanagersaas.service.email.MailDeliveryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Service
public class AssinaturaEmailService {

    private static final String TEMPLATE_PATH = "templates/emails/assinatura-status.html";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final MailDeliveryService mailDeliveryService;
    private final EmailTemplateService templateService;
    private final UsuarioRepository usuarioRepository;
    private final int reminderDays;

    public AssinaturaEmailService(
            MailDeliveryService mailDeliveryService,
            EmailTemplateService templateService,
            UsuarioRepository usuarioRepository,
            @Value("${app.assinatura.reminder-days:7}") int reminderDays
    ) {
        this.mailDeliveryService = mailDeliveryService;
        this.templateService = templateService;
        this.usuarioRepository = usuarioRepository;
        this.reminderDays = reminderDays;
    }

    public void sendCreatedNotification(Assinatura assinatura) {
        sendToAdmins(assinatura, "Assinatura criada", "A assinatura do condomínio foi criada com sucesso.");
    }

    public void sendPlanChangedNotification(Assinatura assinatura) {
        sendToAdmins(assinatura, "Plano da assinatura alterado", "O plano da assinatura foi atualizado.");
    }

    public void sendStatusChangedNotification(Assinatura assinatura) {
        sendToAdmins(assinatura, "Status da assinatura atualizado", "O status da assinatura foi atualizado.");
    }

    public void sendDueSoonNotification(Assinatura assinatura) {
        sendToAdmins(assinatura, "Assinatura próxima do vencimento", "A assinatura está próxima do vencimento.");
    }

    public void sendOverdueNotification(Assinatura assinatura) {
        sendToAdmins(assinatura, "Assinatura vencida", "A assinatura está com vencimento expirado.");
    }

    public int getReminderDays() {
        return reminderDays;
    }

    private void sendToAdmins(Assinatura assinatura, String subject, String headline) {
        Set<String> recipients = new LinkedHashSet<>();
        usuarioRepository.findActiveAdminEmailsByCondominioId(assinatura.getCondominio().getId()).stream()
                .map(this::normalizeEmail)
                .filter(email -> email != null)
                .forEach(recipients::add);

        if (recipients.isEmpty()) {
            return;
        }

        String html = templateService.render(
                TEMPLATE_PATH,
                Map.of(
                        "headline", headline,
                        "nomeCondominio", assinatura.getCondominio().getNome(),
                        "nomePlano", assinatura.getPlano().getNome(),
                        "statusAssinatura", humanizeStatus(assinatura.getStatus()),
                        "dataInicio", formatDate(assinatura.getDataInicio()),
                        "dataVencimento", formatDate(assinatura.getDataVencimento()),
                        "diasParaVencer", String.valueOf(daysUntilDue(assinatura.getDataVencimento()))
                ),
                "Template de e-mail de assinatura não encontrado."
        );

        mailDeliveryService.sendHtml(recipients, assinatura.getCondominio().getNome() + " - " + subject, html, "notificar assinatura");
    }

    private String formatDate(LocalDate date) {
        return date == null ? "" : date.format(DATE_FORMATTER);
    }

    private long daysUntilDue(LocalDate dueDate) {
        if (dueDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
    }

    private String humanizeStatus(AssinaturaStatus status) {
        if (status == null) {
            return "";
        }

        return switch (status) {
            case ATIVO -> "Ativo";
            case PENDENTE -> "Pendente";
            case INADIMPLENTE -> "Inadimplente";
            case CANCELADO -> "Cancelado";
        };
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String normalized = email.trim().toLowerCase();
        return normalized.isBlank() ? null : normalized;
    }
}
