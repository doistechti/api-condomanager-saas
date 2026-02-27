package br.com.doistech.apicondomanagersaas.domain.lead;

/**
 * Status do lead (pré-cadastro).
 *
 * - NOVO: acabou de chegar pela landing
 * - CONTATADO: alguém do time entrou em contato
 * - TRIAL_LIBERADO: acesso de testes liberado
 * - CONVERTIDO: virou cliente
 * - DESCARTADO: não faz sentido seguir
 */
public enum LeadStatus {
    NOVO,
    CONTATADO,
    TRIAL_LIBERADO,
    CONVERTIDO,
    DESCARTADO
}

