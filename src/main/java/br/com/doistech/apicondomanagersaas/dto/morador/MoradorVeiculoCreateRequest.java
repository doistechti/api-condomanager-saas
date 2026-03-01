package br.com.doistech.apicondomanagersaas.dto.morador;

import jakarta.validation.constraints.NotBlank;

/**
 * ✅ Morador NÃO envia pessoaId/condominioId.
 * O backend fixa isso pelo escopo do usuário.
 */
public record MoradorVeiculoCreateRequest(
        @NotBlank String placa,
        String modelo,
        String cor,
        String tipo
) {}
