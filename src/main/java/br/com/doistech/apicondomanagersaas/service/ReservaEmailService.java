package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.domain.reserva.Reserva;
import br.com.doistech.apicondomanagersaas.domain.reserva.ReservaStatus;
import br.com.doistech.apicondomanagersaas.repository.UsuarioRepository;
import br.com.doistech.apicondomanagersaas.service.email.EmailTemplateService;
import br.com.doistech.apicondomanagersaas.service.email.MailDeliveryService;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Service
public class ReservaEmailService {

    private static final String MORADOR_TEMPLATE = "templates/emails/reserva-morador-status.html";
    private static final String ADMIN_TEMPLATE = "templates/emails/reserva-admin-alert.html";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final MailDeliveryService mailDeliveryService;
    private final EmailTemplateService templateService;
    private final UsuarioRepository usuarioRepository;

    public ReservaEmailService(
            MailDeliveryService mailDeliveryService,
            EmailTemplateService templateService,
            UsuarioRepository usuarioRepository
    ) {
        this.mailDeliveryService = mailDeliveryService;
        this.templateService = templateService;
        this.usuarioRepository = usuarioRepository;
    }

    public void sendCreatedNotifications(Reserva reserva) {
        sendMoradorEmail(reserva, buildMoradorSubject(reserva), buildMoradorHeadline(reserva), buildMoradorBodyMessage(reserva));
        sendAdminEmail(reserva, "Nova reserva " + describeStatus(reserva.getStatus()), "Nova solicitacao de reserva");
    }

    public void sendStatusUpdatedNotifications(Reserva reserva) {
        sendMoradorEmail(reserva, buildMoradorSubject(reserva), buildMoradorHeadline(reserva), buildMoradorBodyMessage(reserva));
        sendAdminEmail(reserva, "Reserva atualizada - " + describeStatus(reserva.getStatus()), "Status da reserva atualizado");
    }

    public void sendCancelledNotifications(Reserva reserva) {
        sendMoradorEmail(
                reserva,
                buildMoradorSubjectForCancellation(reserva),
                "Reserva cancelada",
                "Seu agendamento foi cancelado. Se quiser, você pode fazer uma nova solicitação quando for melhor para você."
        );
        sendAdminEmail(reserva, "Reserva cancelada", "Reserva cancelada");
    }

    private void sendMoradorEmail(Reserva reserva, String subject, String headline, String bodyMessage) {
        String email = normalizeEmail(reserva.getVinculo().getPessoa().getEmail());
        if (email == null) {
            return;
        }

        String html = templateService.render(
                MORADOR_TEMPLATE,
                buildMoradorTemplateVariables(reserva, headline, bodyMessage),
                "Template de e-mail da reserva do morador não encontrado."
        );
        mailDeliveryService.sendHtml(email, subject, html, "notificar a reserva do morador");
    }

    private void sendAdminEmail(Reserva reserva, String subject, String headline) {
        Set<String> recipients = new LinkedHashSet<>();
        usuarioRepository.findActiveAdminEmailsByCondominioId(reserva.getCondominio().getId()).stream()
                .map(this::normalizeEmail)
                .filter(email -> email != null)
                .forEach(recipients::add);

        if (recipients.isEmpty()) {
            return;
        }

        String html = templateService.render(
                ADMIN_TEMPLATE,
                buildTemplateVariables(reserva, headline, describeStatus(reserva.getStatus())),
                "Template de e-mail administrativo da reserva não encontrado."
        );
        mailDeliveryService.sendHtml(recipients, subject, html, "notificar a reserva para administradores");
    }

    private Map<String, String> buildMoradorTemplateVariables(Reserva reserva, String headline, String bodyMessage) {
        return Map.ofEntries(
                Map.entry("headline", headline),
                Map.entry("bodyMessage", bodyMessage),
                Map.entry("statusReserva", describeStatus(reserva.getStatus())),
                Map.entry("statusBadgeColor", resolveStatusBadgeColor(reserva.getStatus())),
                Map.entry("statusBadgeBackground", resolveStatusBadgeBackground(reserva.getStatus())),
                Map.entry("nomeCondominio", reserva.getCondominio().getNome()),
                Map.entry("nomeEspaco", reserva.getEspaco().getNome()),
                Map.entry("nomeMorador", reserva.getVinculo().getPessoa().getNome()),
                Map.entry("identificacaoUnidade", reserva.getVinculo().getUnidade().getIdentificacao()),
                Map.entry("dataReserva", reserva.getDataReserva().format(DATE_FORMATTER)),
                Map.entry("horarioReserva", formatTimeRange(reserva)),
                Map.entry("observacoes", resolveOptionalText(reserva.getObservacoes(), "Sem observações informadas.")),
                Map.entry("motivoRecusa", resolveOptionalText(reserva.getMotivoRecusa(), "Nenhum motivo informado."))
        );
    }

    private Map<String, String> buildTemplateVariables(Reserva reserva, String headline, String statusLabel) {
        return Map.of(
                "headline", headline,
                "statusReserva", statusLabel,
                "nomeCondominio", reserva.getCondominio().getNome(),
                "nomeEspaco", reserva.getEspaco().getNome(),
                "nomeMorador", reserva.getVinculo().getPessoa().getNome(),
                "identificacaoUnidade", reserva.getVinculo().getUnidade().getIdentificacao(),
                "dataReserva", reserva.getDataReserva().format(DATE_FORMATTER),
                "horarioReserva", formatTimeRange(reserva),
                "observacoes", blankIfNull(reserva.getObservacoes()),
                "motivoRecusa", blankIfNull(reserva.getMotivoRecusa())
        );
    }

    private String buildMoradorSubject(Reserva reserva) {
        return reserva.getCondominio().getNome() + " - Reserva " + describeStatus(reserva.getStatus());
    }

    private String buildMoradorSubjectForCancellation(Reserva reserva) {
        return reserva.getCondominio().getNome() + " - Reserva cancelada";
    }

    private String buildMoradorHeadline(Reserva reserva) {
        if (reserva.getStatus() == null) {
            return "Sua reserva foi atualizada";
        }

        return switch (reserva.getStatus()) {
            case PENDENTE -> "Recebemos seu pedido de reserva";
            case APROVADA -> "Obaaa, vai rolar a festa! Sua reserva foi aprovada.";
            case RECUSADA -> "Poxa, dessa vez não deu. Sua reserva não foi aprovada.";
            case CANCELADA -> "Reserva cancelada";
        };
    }

    private String buildMoradorBodyMessage(Reserva reserva) {
        if (reserva.getStatus() == null) {
            return "Te avisamos por aqui sempre que tiver alguma novidade na sua reserva.";
        }

        return switch (reserva.getStatus()) {
            case PENDENTE -> "Seu pedido foi enviado e agora está aguardando a confirmação da administração do condomínio.";
            case APROVADA -> "Pode comemorar e se organizar com tranquilidade. O espaço está reservado para você na data escolhida.";
            case RECUSADA -> "Vale a pena conferir o motivo informado abaixo e, se precisar, fazer uma nova solicitação com outro horário ou data.";
            case CANCELADA -> "Seu agendamento foi cancelado. Se precisar, é só fazer uma nova reserva quando quiser.";
        };
    }

    private String describeStatus(ReservaStatus status) {
        if (status == null) {
            return "atualizada";
        }
        return switch (status) {
            case PENDENTE -> "pendente";
            case APROVADA -> "aprovada";
            case RECUSADA -> "recusada";
            case CANCELADA -> "cancelada";
        };
    }

    private String formatTimeRange(Reserva reserva) {
        if (reserva.getHoraInicio() == null && reserva.getHoraFim() == null) {
            return "Dia inteiro";
        }
        String inicio = reserva.getHoraInicio() == null ? "--:--" : reserva.getHoraInicio().format(TIME_FORMATTER);
        String fim = reserva.getHoraFim() == null ? "--:--" : reserva.getHoraFim().format(TIME_FORMATTER);
        return inicio + " até " + fim;
    }

    private String resolveStatusBadgeColor(ReservaStatus status) {
        if (status == null) {
            return "#1f2937";
        }
        return switch (status) {
            case PENDENTE -> "#92400e";
            case APROVADA -> "#166534";
            case RECUSADA -> "#991b1b";
            case CANCELADA -> "#374151";
        };
    }

    private String resolveStatusBadgeBackground(ReservaStatus status) {
        if (status == null) {
            return "#e5e7eb";
        }
        return switch (status) {
            case PENDENTE -> "#fef3c7";
            case APROVADA -> "#dcfce7";
            case RECUSADA -> "#fee2e2";
            case CANCELADA -> "#e5e7eb";
        };
    }

    private String resolveOptionalText(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }

    private String blankIfNull(String value) {
        return value == null ? "" : value;
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String normalized = email.trim().toLowerCase();
        return normalized.isBlank() ? null : normalized;
    }
}
