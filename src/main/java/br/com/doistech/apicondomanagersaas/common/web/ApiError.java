package br.com.doistech.apicondomanagersaas.common.web;

import java.time.Instant;

/**
 * Payload padrão de erro.
 */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
