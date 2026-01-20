package br.com.doistech.apicondomanagersaas.common.exception;

/**
 * Exceção padrão para recurso não encontrado.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
