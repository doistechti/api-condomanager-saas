package br.com.doistech.apicondomanagersaas.controller;

import br.com.doistech.apicondomanagersaas.dto.notificacao.PushPublicKeyResponse;
import br.com.doistech.apicondomanagersaas.dto.notificacao.PushSubscriptionRequest;
import br.com.doistech.apicondomanagersaas.dto.notificacao.PushUnsubscribeRequest;
import br.com.doistech.apicondomanagersaas.service.push.PushSubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/push")
@RequiredArgsConstructor
public class PushNotificationController {

    private final PushSubscriptionService pushSubscriptionService;

    @GetMapping("/public-key")
    public PushPublicKeyResponse getPublicKey() {
        return new PushPublicKeyResponse(pushSubscriptionService.getPublicKey());
    }

    @PostMapping("/subscriptions")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void saveSubscription(Authentication authentication, @Valid @RequestBody PushSubscriptionRequest request) {
        pushSubscriptionService.saveSubscription(authentication.getName(), request);
    }

    @PostMapping("/subscriptions/unsubscribe")
    public ResponseEntity<Void> unsubscribe(Authentication authentication, @Valid @RequestBody PushUnsubscribeRequest request) {
        pushSubscriptionService.unsubscribe(authentication.getName(), request.endpoint());
        return ResponseEntity.noContent().build();
    }
}
