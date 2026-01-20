package br.com.doistech.apicondomanagersaas.common.exception;

/**
 * Exceção para regras de negócio/validações simples.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
