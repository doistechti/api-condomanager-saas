package br.com.doistech.apicondomanagersaas.service.push;

public record PushMessage(
        String title,
        String body,
        String url,
        String tag
) {
}
