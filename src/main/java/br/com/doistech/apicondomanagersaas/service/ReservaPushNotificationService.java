package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.domain.reserva.Reserva;
import br.com.doistech.apicondomanagersaas.domain.reserva.ReservaStatus;
import br.com.doistech.apicondomanagersaas.repository.UsuarioRepository;
import br.com.doistech.apicondomanagersaas.service.push.PushMessage;
import br.com.doistech.apicondomanagersaas.service.push.PushNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservaPushNotificationService {

    private final PushNotificationService pushNotificationService;
    private final UsuarioRepository usuarioRepository;

    public void sendCreatedNotifications(Reserva reserva) {
        pushNotificationService.sendToUser(
                getMoradorUsuarioId(reserva),
                new PushMessage(
                        buildMoradorTitle(reserva),
                        buildMoradorBody(reserva),
                        "/morador/reservas",
                        "reserva-" + reserva.getId()
                )
        );

        if (reserva.getStatus() == ReservaStatus.PENDENTE) {
            pushNotificationService.sendToCondominioAdmins(
                    reserva.getCondominio().getId(),
                    new PushMessage(
                            "Nova reserva pendente",
                            reserva.getVinculo().getPessoa().getNome() + " solicitou " + reserva.getEspaco().getNome() + ".",
                            "/admin-condominio/reservas",
                            "admin-reserva-" + reserva.getId()
                    )
            );
        }
    }

    public void sendStatusUpdatedNotifications(Reserva reserva) {
        pushNotificationService.sendToUser(
                getMoradorUsuarioId(reserva),
                new PushMessage(
                        buildMoradorTitle(reserva),
                        buildMoradorBody(reserva),
                        "/morador/reservas",
                        "reserva-" + reserva.getId()
                )
        );
    }

    public void sendCancelledNotifications(Reserva reserva) {
        pushNotificationService.sendToUser(
                getMoradorUsuarioId(reserva),
                new PushMessage(
                        "Reserva cancelada",
                        "Sua reserva de " + reserva.getEspaco().getNome() + " foi cancelada.",
                        "/morador/reservas",
                        "reserva-" + reserva.getId()
                )
        );
    }

    private String buildMoradorTitle(Reserva reserva) {
        if (reserva.getStatus() == null) {
            return "Reserva atualizada";
        }

        return switch (reserva.getStatus()) {
            case PENDENTE -> "Reserva em análise";
            case APROVADA -> "Reserva aprovada";
            case RECUSADA -> "Reserva recusada";
            case CANCELADA -> "Reserva cancelada";
        };
    }

    private String buildMoradorBody(Reserva reserva) {
        if (reserva.getStatus() == null) {
            return "A sua reserva recebeu uma atualização.";
        }

        return switch (reserva.getStatus()) {
            case PENDENTE -> "Recebemos seu pedido para " + reserva.getEspaco().getNome() + ".";
            case APROVADA -> "Obaaa, vai rolar! Sua reserva de " + reserva.getEspaco().getNome() + " foi aprovada.";
            case RECUSADA -> "Sua reserva de " + reserva.getEspaco().getNome() + " não foi aprovada.";
            case CANCELADA -> "Sua reserva de " + reserva.getEspaco().getNome() + " foi cancelada.";
        };
    }

    private Long getMoradorUsuarioId(Reserva reserva) {
        if (reserva.getVinculo() == null
                || reserva.getVinculo().getPessoa() == null
                || reserva.getVinculo().getPessoa().getEmail() == null
                || reserva.getVinculo().getPessoa().getEmail().isBlank()) {
            return null;
        }
        return usuarioRepository.findByEmail(reserva.getVinculo().getPessoa().getEmail().trim().toLowerCase())
                .map(usuario -> usuario.getId())
                .orElse(null);
    }
}
