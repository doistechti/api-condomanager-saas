package br.com.doistech.apicondomanagersaas.service.push;

import br.com.doistech.apicondomanagersaas.repository.PushSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedHashSet;

@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final WebPushDeliveryService webPushDeliveryService;

    public void sendToUser(Long usuarioId, PushMessage message) {
        if (usuarioId == null) {
            return;
        }
        webPushDeliveryService.send(pushSubscriptionRepository.findAllByUsuarioIdAndAtivoTrue(usuarioId), message);
    }

    public void sendToUsers(Collection<Long> usuarioIds, PushMessage message) {
        if (usuarioIds == null || usuarioIds.isEmpty()) {
            return;
        }
        webPushDeliveryService.send(
                pushSubscriptionRepository.findAllByUsuarioIdInAndAtivoTrue(new LinkedHashSet<>(usuarioIds)),
                message
        );
    }

    public void sendToCondominioAdmins(Long condominioId, PushMessage message) {
        if (condominioId == null) {
            return;
        }
        webPushDeliveryService.send(pushSubscriptionRepository.findActiveAdminSubscriptionsByCondominioId(condominioId), message);
    }
}
