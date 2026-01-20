package br.com.doistech.apicondomanagersaas.common.web;

/**
 * Placeholder para multi-tenant via filtro JWT no futuro.
 * Por enquanto voce pode preencher manualmente nos controllers.
 */
public final class RequestContext {
    private static final ThreadLocal<Long> CONDOMINIO_ID = new ThreadLocal<>();

    private RequestContext() {}

    public static void setCondominioId(Long condominioId) {
        CONDOMINIO_ID.set(condominioId);
    }

    public static Long getCondominioId() {
        return CONDOMINIO_ID.get();
    }

    public static void clear() {
        CONDOMINIO_ID.remove();
    }
}
