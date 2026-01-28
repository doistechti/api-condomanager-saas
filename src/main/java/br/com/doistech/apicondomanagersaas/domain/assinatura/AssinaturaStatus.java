package br.com.doistech.apicondomanagersaas.domain.assinatura;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AssinaturaStatus {
    ATIVO,
    PENDENTE,
    INADIMPLENTE,
    CANCELADO;

    @JsonValue
    public String toJson() {
        return this.name().toLowerCase();
    }

    @JsonCreator
    public static AssinaturaStatus fromJson(String value) {
        if (value == null) return null;
        return AssinaturaStatus.valueOf(value.trim().toUpperCase());
    }
}


