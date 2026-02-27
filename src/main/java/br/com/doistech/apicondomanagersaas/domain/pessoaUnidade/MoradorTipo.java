package br.com.doistech.apicondomanagersaas.domain.pessoaUnidade;

/**
 * Tipo do vínculo quando ehMorador=true.
 * Mantém compatibilidade com a UI: proprietario/inquilino/dependente.
 */
public enum MoradorTipo {
    PROPRIETARIO,
    INQUILINO,
    DEPENDENTE
}
