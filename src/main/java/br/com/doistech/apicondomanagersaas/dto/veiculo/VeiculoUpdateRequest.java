package br.com.doistech.apicondomanagersaas.dto.veiculo;

import jakarta.validation.constraints.NotBlank;

public record VeiculoUpdateRequest(
        @NotBlank String placa,
        String modelo,
        String cor,
        String tipo
) {
}
