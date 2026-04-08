package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.domain.ocorrencia.Ocorrencia;
import br.com.doistech.apicondomanagersaas.domain.ocorrencia.OcorrenciaMensagem;
import br.com.doistech.apicondomanagersaas.domain.ocorrencia.OcorrenciaStatus;
import br.com.doistech.apicondomanagersaas.service.push.PushMessage;
import br.com.doistech.apicondomanagersaas.service.push.PushNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OcorrenciaPushNotificationService {

    private final PushNotificationService pushNotificationService;

    public void sendCreatedNotifications(Ocorrencia ocorrencia) {
        pushNotificationService.sendToCondominioAdmins(
                ocorrencia.getCondominio().getId(),
                new PushMessage(
                        "Nova ocorrência aberta",
                        ocorrencia.getMoradorVinculo().getPessoa().getNome() + " abriu a ocorrência " + ocorrencia.getCodigo() + ".",
                        "/admin-condominio/ocorrencias",
                        "admin-ocorrencia-" + ocorrencia.getId()
                )
        );
    }

    public void sendAdminReplyNotifications(Ocorrencia ocorrencia, OcorrenciaMensagem mensagem) {
        pushNotificationService.sendToUser(
                getMoradorUsuarioId(ocorrencia),
                new PushMessage(
                        "Tem resposta na sua ocorrência",
                        truncateMessage(mensagem.getMensagem(), "A administração respondeu sua ocorrência."),
                        "/morador/ocorrencias",
                        "ocorrencia-" + ocorrencia.getId()
                )
        );
    }

    public void sendMoradorReplyNotifications(Ocorrencia ocorrencia, OcorrenciaMensagem mensagem) {
        pushNotificationService.sendToCondominioAdmins(
                ocorrencia.getCondominio().getId(),
                new PushMessage(
                        "Nova resposta do morador",
                        truncateMessage(mensagem.getMensagem(), "O morador respondeu a ocorrência " + ocorrencia.getCodigo() + "."),
                        "/admin-condominio/ocorrencias",
                        "admin-ocorrencia-" + ocorrencia.getId()
                )
        );
    }

    public void sendStatusUpdatedNotifications(Ocorrencia ocorrencia) {
        pushNotificationService.sendToUser(
                getMoradorUsuarioId(ocorrencia),
                new PushMessage(
                        buildMoradorTitle(ocorrencia),
                        buildMoradorBody(ocorrencia),
                        "/morador/ocorrencias",
                        "ocorrencia-" + ocorrencia.getId()
                )
        );
    }

    private String buildMoradorTitle(Ocorrencia ocorrencia) {
        if (ocorrencia.getStatus() == null) {
            return "Ocorrência atualizada";
        }

        return switch (ocorrencia.getStatus()) {
            case aberta -> "Ocorrência aberta";
            case em_analise -> "Ocorrência em análise";
            case aguardando_morador -> "Precisamos da sua resposta";
            case respondida -> "Tem novidade na ocorrência";
            case resolvida -> "Ocorrência resolvida";
            case cancelada -> "Ocorrência cancelada";
        };
    }

    private String buildMoradorBody(Ocorrencia ocorrencia) {
        if (ocorrencia.getStatus() == null) {
            return "Sua ocorrência recebeu uma atualização.";
        }

        return switch (ocorrencia.getStatus()) {
            case aberta -> "Seu relato está em acompanhamento.";
            case em_analise -> "A administração está analisando a ocorrência.";
            case aguardando_morador -> "A administração precisa de mais informações suas.";
            case respondida -> "A administração atualizou a ocorrência.";
            case resolvida -> "Boa notícia: sua ocorrência foi resolvida.";
            case cancelada -> "A ocorrência foi encerrada como cancelada.";
        };
    }

    private String truncateMessage(String message, String fallback) {
        if (message == null || message.isBlank()) {
            return fallback;
        }

        String normalized = message.trim();
        return normalized.length() > 120 ? normalized.substring(0, 117) + "..." : normalized;
    }

    private Long getMoradorUsuarioId(Ocorrencia ocorrencia) {
        if (ocorrencia.getMoradorVinculo() == null || ocorrencia.getMoradorVinculo().getUsuario() == null) {
            return null;
        }
        return ocorrencia.getMoradorVinculo().getUsuario().getId();
    }
}
