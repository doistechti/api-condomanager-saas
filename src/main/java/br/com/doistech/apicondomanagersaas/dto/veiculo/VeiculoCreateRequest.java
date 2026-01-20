package br.com.doistech.apicondomanagersaas.dto.veiculo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VeiculoCreateRequest(
        @NotNull Long condominioId,
        @NotNull Long pessoaId,
        @NotBlank String placa,
        String modelo,
        String cor,
        String tipo
) {
}
