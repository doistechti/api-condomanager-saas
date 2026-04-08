package br.com.doistech.apicondomanagersaas.service.push;

import br.com.doistech.apicondomanagersaas.common.exception.BadRequestException;
import br.com.doistech.apicondomanagersaas.common.exception.NotFoundException;
import br.com.doistech.apicondomanagersaas.domain.notificacao.PushSubscription;
import br.com.doistech.apicondomanagersaas.domain.usuario.Usuario;
import br.com.doistech.apicondomanagersaas.dto.notificacao.PushSubscriptionRequest;
import br.com.doistech.apicondomanagersaas.repository.PushSubscriptionRepository;
import br.com.doistech.apicondomanagersaas.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PushSubscriptionService {

    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final UsuarioRepository usuarioRepository;

    @Value("${app.push.vapid.public-key:}")
    private String publicKey;

    @Transactional
    public void saveSubscription(String email, PushSubscriptionRequest request) {
        Usuario usuario = getUsuario(email);
        LocalDateTime now = LocalDateTime.now();

        PushSubscription subscription = pushSubscriptionRepository.findByEndpoint(request.endpoint())
                .orElseGet(PushSubscription::new);

        if (subscription.getId() == null) {
            subscription.setCreatedAt(now);
        }

        subscription.setUsuario(usuario);
        subscription.setEndpoint(request.endpoint());
        subscription.setP256dhKey(request.keys().p256dh());
        subscription.setAuthKey(request.keys().auth());
        subscription.setExpirationTime(request.expirationTime());
        subscription.setUserAgent(trimToNull(request.userAgent()));
        subscription.setAtivo(true);
        subscription.setUpdatedAt(now);
        subscription.setLastFailureAt(null);
        subscription.setLastFailureReason(null);

        pushSubscriptionRepository.save(subscription);
    }

    @Transactional
    public void unsubscribe(String email, String endpoint) {
        Usuario usuario = getUsuario(email);

        pushSubscriptionRepository.findByEndpoint(endpoint)
                .filter(subscription -> subscription.getUsuario().getId().equals(usuario.getId()))
                .ifPresent(pushSubscriptionRepository::delete);
    }

    public String getPublicKey() {
        if (publicKey == null || publicKey.isBlank()) {
            throw new BadRequestException("Push notification não configurado.");
        }
        return publicKey;
    }

    private Usuario getUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado."));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isBlank() ? null : normalized;
    }
}
