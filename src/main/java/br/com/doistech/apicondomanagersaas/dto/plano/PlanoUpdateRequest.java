package br.com.doistech.apicondomanagersaas.dto.plano;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record PlanoUpdateRequest(
        @NotBlank String nome,
        @NotNull BigDecimal preco,
        @NotNull Integer maxUnidades,
        @NotNull Integer maxAdmins,
        String descricao,
        List<String> recursos,
        @NotNull Boolean destaque,
        @NotNull Boolean ativo
) {}