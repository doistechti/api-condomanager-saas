package br.com.doistech.apicondomanagersaas.service.push;

import br.com.doistech.apicondomanagersaas.domain.notificacao.PushSubscription;
import br.com.doistech.apicondomanagersaas.repository.PushSubscriptionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebPushDeliveryService {

    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.push.vapid.public-key:}")
    private String publicKey;

    @Value("${app.push.vapid.private-key:}")
    private String privateKey;

    @Value("${app.push.vapid.subject:mailto:suporte@doistech.com.br}")
    private String subject;

    @Async("mailTaskExecutor")
    @Transactional
    public void send(Collection<PushSubscription> subscriptions, PushMessage message) {
        if (!isConfigured()) {
            log.warn("Push notification não configurado. Mensagem ignorada: {}", message.title());
            return;
        }

        Set<Long> processedIds = new LinkedHashSet<>();
        for (PushSubscription subscription : subscriptions) {
            if (subscription == null || subscription.getId() == null || !processedIds.add(subscription.getId())) {
                continue;
            }
            sendToSingleSubscription(subscription, message);
        }
    }

    private void sendToSingleSubscription(PushSubscription subscription, PushMessage message) {
        try {
            Notification notification = new Notification(
                    subscription.getEndpoint(),
                    subscription.getP256dhKey(),
                    subscription.getAuthKey(),
                    buildPayload(message).getBytes(StandardCharsets.UTF_8)
            );

            HttpResponse response = buildPushService().send(notification);
            int status = response.getStatusLine().getStatusCode();

            if (status == 404 || status == 410) {
                pushSubscriptionRepository.delete(subscription);
                return;
            }

            if (status >= 200 && status < 300) {
                markSuccess(subscription);
                return;
            }

            markFailure(subscription, "HTTP " + status);
        } catch (Exception ex) {
            log.warn("Falha ao enviar push para subscription {}: {}", subscription.getId(), ex.getMessage());
            markFailure(subscription, ex.getClass().getSimpleName());
        }
    }

    private String buildPayload(PushMessage message) throws JsonProcessingException {
        return objectMapper.writeValueAsString(Map.of(
                "title", message.title(),
                "body", message.body(),
                "url", message.url(),
                "tag", message.tag()
        ));
    }

    private PushService buildPushService() throws Exception {
        return new PushService()
                .setSubject(subject)
                .setPublicKey(publicKey)
                .setPrivateKey(privateKey);
    }

    private void markSuccess(PushSubscription subscription) {
        subscription.setAtivo(true);
        subscription.setUpdatedAt(LocalDateTime.now());
        subscription.setLastSuccessAt(LocalDateTime.now());
        subscription.setLastFailureAt(null);
        subscription.setLastFailureReason(null);
        pushSubscriptionRepository.save(subscription);
    }

    private void markFailure(PushSubscription subscription, String reason) {
        subscription.setUpdatedAt(LocalDateTime.now());
        subscription.setLastFailureAt(LocalDateTime.now());
        subscription.setLastFailureReason(reason);
        pushSubscriptionRepository.save(subscription);
    }

    private boolean isConfigured() {
        return publicKey != null && !publicKey.isBlank()
                && privateKey != null && !privateKey.isBlank()
                && subject != null && !subject.isBlank();
    }
}
