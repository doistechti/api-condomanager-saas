package br.com.doistech.apicondomanagersaas.domain.pessoaUnidade;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

/**
 * Tipo do vinculo quando ehMorador=true.
 * Mantem compatibilidade com a UI: proprietario/inquilino/dependente.
 */
public enum MoradorTipo {
    PROPRIETARIO,
    INQUILINO,
    DEPENDENTE;

    @JsonValue
    public String toJson() {
        return name().toLowerCase(Locale.ROOT);
    }

    @JsonCreator
    public static MoradorTipo fromJson(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return MoradorTipo.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
