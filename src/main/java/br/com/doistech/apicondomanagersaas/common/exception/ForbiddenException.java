package br.com.doistech.apicondomanagersaas.common.exception;

/**
 * Exceção para quando o usuário não tem permissão para seguir com a operação.
 *
 * Neste projeto, usamos para bloquear login de contas pendentes (trial ainda não liberado).
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
