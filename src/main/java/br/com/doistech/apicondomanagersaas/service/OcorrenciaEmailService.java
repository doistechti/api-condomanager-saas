package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.domain.ocorrencia.Ocorrencia;
import br.com.doistech.apicondomanagersaas.domain.ocorrencia.OcorrenciaMensagem;
import br.com.doistech.apicondomanagersaas.domain.ocorrencia.OcorrenciaStatus;
import br.com.doistech.apicondomanagersaas.repository.UsuarioRepository;
import br.com.doistech.apicondomanagersaas.service.email.EmailTemplateService;
import br.com.doistech.apicondomanagersaas.service.email.MailDeliveryService;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Service
public class OcorrenciaEmailService {

    private static final String MORADOR_TEMPLATE = "templates/emails/ocorrencia-morador-update.html";
    private static final String ADMIN_TEMPLATE = "templates/emails/ocorrencia-admin-alert.html";

    private final MailDeliveryService mailDeliveryService;
    private final EmailTemplateService templateService;
    private final UsuarioRepository usuarioRepository;

    public OcorrenciaEmailService(
            MailDeliveryService mailDeliveryService,
            EmailTemplateService templateService,
            UsuarioRepository usuarioRepository
    ) {
        this.mailDeliveryService = mailDeliveryService;
        this.templateService = templateService;
        this.usuarioRepository = usuarioRepository;
    }

    public void sendCreatedNotifications(Ocorrencia ocorrencia) {
        sendMoradorEmail(
                ocorrencia,
                "Ocorrência registrada",
                "Recebemos sua ocorrência",
                "Seu relato chegou certinho para a administração. Agora é só acompanhar por aqui as próximas atualizações."
        );
        sendAdminEmail(ocorrencia, "Nova ocorrência - " + ocorrencia.getCodigo(), "Nova ocorrência aberta");
    }

    public void sendAdminReplyNotifications(Ocorrencia ocorrencia, OcorrenciaMensagem mensagem) {
        sendMoradorEmail(
                ocorrencia,
                "Nova resposta na ocorrência " + ocorrencia.getCodigo(),
                "Tem novidade na sua ocorrência",
                "A administração acabou de responder seu relato. Vale a pena conferir a mensagem abaixo.",
                mensagem.getMensagem()
        );
        sendAdminEmail(ocorrencia, "Resposta enviada na ocorrência " + ocorrencia.getCodigo(), "Resposta administrativa registrada", mensagem.getMensagem());
    }

    public void sendMoradorReplyNotifications(Ocorrencia ocorrencia, OcorrenciaMensagem mensagem) {
        sendMoradorEmail(
                ocorrencia,
                "Resposta enviada na ocorrência " + ocorrencia.getCodigo(),
                "Sua mensagem foi enviada",
                "Registramos sua resposta e ela já ficou disponível para a administração acompanhar.",
                mensagem.getMensagem()
        );
        sendAdminEmail(ocorrencia, "Morador respondeu à ocorrência " + ocorrencia.getCodigo(), "Nova resposta do morador", mensagem.getMensagem());
    }

    public void sendStatusUpdatedNotifications(Ocorrencia ocorrencia) {
        sendMoradorEmail(
                ocorrencia,
                "Status da ocorrência atualizado",
                buildMoradorHeadlineForStatus(ocorrencia),
                buildMoradorBodyMessageForStatus(ocorrencia)
        );
        sendAdminEmail(ocorrencia, "Status da ocorrência atualizado", "Status da ocorrência atualizado");
    }

    private void sendMoradorEmail(Ocorrencia ocorrencia, String subject, String headline) {
        sendMoradorEmail(ocorrencia, subject, headline, "", "");
    }

    private void sendMoradorEmail(Ocorrencia ocorrencia, String subject, String headline, String bodyMessage) {
        sendMoradorEmail(ocorrencia, subject, headline, bodyMessage, "");
    }

    private void sendMoradorEmail(
            Ocorrencia ocorrencia,
            String subject,
            String headline,
            String bodyMessage,
            String latestMessage
    ) {
        String email = normalizeEmail(ocorrencia.getMoradorVinculo().getPessoa().getEmail());
        if (email == null) {
            return;
        }

        String html = templateService.render(
                MORADOR_TEMPLATE,
                buildMoradorTemplateVariables(ocorrencia, headline, bodyMessage, latestMessage),
                "Template de e-mail da ocorrência do morador não encontrado."
        );
        mailDeliveryService.sendHtml(email, subject, html, "notificar a ocorrência do morador");
    }

    private void sendAdminEmail(Ocorrencia ocorrencia, String subject, String headline) {
        sendAdminEmail(ocorrencia, subject, headline, "");
    }

    private void sendAdminEmail(Ocorrencia ocorrencia, String subject, String headline, String latestMessage) {
        Set<String> recipients = new LinkedHashSet<>();
        usuarioRepository.findActiveAdminEmailsByCondominioId(ocorrencia.getCondominio().getId()).stream()
                .map(this::normalizeEmail)
                .filter(email -> email != null)
                .forEach(recipients::add);

        if (recipients.isEmpty()) {
            return;
        }

        String html = templateService.render(
                ADMIN_TEMPLATE,
                buildTemplateVariables(ocorrencia, headline, latestMessage),
                "Template de e-mail administrativo da ocorrência não encontrado."
        );
        mailDeliveryService.sendHtml(recipients, subject, html, "notificar a ocorrência para administradores");
    }

    private Map<String, String> buildMoradorTemplateVariables(
            Ocorrencia ocorrencia,
            String headline,
            String bodyMessage,
            String latestMessage
    ) {
        return Map.ofEntries(
                Map.entry("headline", headline),
                Map.entry("bodyMessage", bodyMessage),
                Map.entry("codigoOcorrencia", ocorrencia.getCodigo()),
                Map.entry("statusOcorrencia", humanizeStatus(ocorrencia.getStatus())),
                Map.entry("statusBadgeColor", resolveStatusBadgeColor(ocorrencia.getStatus())),
                Map.entry("statusBadgeBackground", resolveStatusBadgeBackground(ocorrencia.getStatus())),
                Map.entry("categoriaOcorrencia", humanizeSlug(ocorrencia.getCategoria().name())),
                Map.entry("tituloOcorrencia", ocorrencia.getTitulo()),
                Map.entry("descricaoOcorrencia", resolveOptionalText(ocorrencia.getDescricao(), "Sem descrição complementar.")),
                Map.entry("localOcorrencia", resolveOptionalText(ocorrencia.getLocalOcorrencia(), "Não informado.")),
                Map.entry("nomeCondominio", ocorrencia.getCondominio().getNome()),
                Map.entry("nomeMorador", ocorrencia.getMoradorVinculo().getPessoa().getNome()),
                Map.entry("identificacaoUnidade", ocorrencia.getUnidade().getIdentificacao()),
                Map.entry("ultimaMensagem", resolveOptionalText(latestMessage, "Ainda não há nova mensagem nesta atualização."))
        );
    }

    private Map<String, String> buildTemplateVariables(Ocorrencia ocorrencia, String headline, String latestMessage) {
        return Map.ofEntries(
                Map.entry("headline", headline),
                Map.entry("codigoOcorrencia", ocorrencia.getCodigo()),
                Map.entry("statusOcorrencia", humanizeStatus(ocorrencia.getStatus())),
                Map.entry("categoriaOcorrencia", humanizeSlug(ocorrencia.getCategoria().name())),
                Map.entry("tituloOcorrencia", ocorrencia.getTitulo()),
                Map.entry("descricaoOcorrencia", blankIfNull(ocorrencia.getDescricao())),
                Map.entry("localOcorrencia", blankIfNull(ocorrencia.getLocalOcorrencia())),
                Map.entry("nomeCondominio", ocorrencia.getCondominio().getNome()),
                Map.entry("nomeMorador", ocorrencia.getMoradorVinculo().getPessoa().getNome()),
                Map.entry("identificacaoUnidade", ocorrencia.getUnidade().getIdentificacao()),
                Map.entry("ultimaMensagem", blankIfNull(latestMessage))
        );
    }

    private String buildMoradorHeadlineForStatus(Ocorrencia ocorrencia) {
        if (ocorrencia.getStatus() == null) {
            return "Sua ocorrência foi atualizada";
        }

        return switch (ocorrencia.getStatus()) {
            case aberta -> "Sua ocorrência voltou para acompanhamento";
            case em_analise -> "Sua ocorrência está em análise";
            case aguardando_morador -> "Precisamos da sua ajuda para seguir";
            case respondida -> "Tem resposta nova na sua ocorrência";
            case resolvida -> "Boa notícia: sua ocorrência foi resolvida";
            case cancelada -> "Sua ocorrência foi cancelada";
        };
    }

    private String buildMoradorBodyMessageForStatus(Ocorrencia ocorrencia) {
        if (ocorrencia.getStatus() == null) {
            return "Te avisamos por aqui sempre que houver uma novidade sobre seu relato.";
        }

        return switch (ocorrencia.getStatus()) {
            case aberta -> "Seu relato segue aberto e em acompanhamento pela administração.";
            case em_analise -> "A administração já está avaliando os detalhes do seu relato.";
            case aguardando_morador -> "Precisamos de mais informações suas para conseguir avançar com a tratativa.";
            case respondida -> "A administração atualizou a ocorrência e deixou uma devolutiva para você.";
            case resolvida -> "A tratativa foi concluída. Se ainda precisar de algo, você pode abrir uma nova ocorrência.";
            case cancelada -> "A ocorrência foi encerrada como cancelada. Se foi um engano, você pode registrar novamente.";
        };
    }

    private String humanizeStatus(OcorrenciaStatus status) {
        return status == null ? "" : humanizeSlug(status.name());
    }

    private String humanizeSlug(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        String normalized = value.replace('_', ' ')
                .replace("analise", "análise")
                .replace("area", "área");
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }

    private String blankIfNull(String value) {
        return value == null ? "" : value;
    }

    private String resolveOptionalText(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }

    private String resolveStatusBadgeColor(OcorrenciaStatus status) {
        if (status == null) {
            return "#1f2937";
        }
        return switch (status) {
            case aberta -> "#1d4ed8";
            case em_analise -> "#92400e";
            case aguardando_morador -> "#7c3aed";
            case respondida -> "#0f766e";
            case resolvida -> "#166534";
            case cancelada -> "#374151";
        };
    }

    private String resolveStatusBadgeBackground(OcorrenciaStatus status) {
        if (status == null) {
            return "#e5e7eb";
        }
        return switch (status) {
            case aberta -> "#dbeafe";
            case em_analise -> "#fef3c7";
            case aguardando_morador -> "#ede9fe";
            case respondida -> "#ccfbf1";
            case resolvida -> "#dcfce7";
            case cancelada -> "#e5e7eb";
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
