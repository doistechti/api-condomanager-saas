package br.com.doistech.apicondomanagersaas.dto.plano;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record PlanoCreateRequest(
        @NotBlank String nome,
        @NotNull BigDecimal preco,
        @NotNull Integer maxUnidades,
        @NotNull Integer maxAdmins,      // front manda 999 p/ ilimitado
        String descricao,
        List<String> recursos,
        @NotNull Boolean destaque,
        @NotNull Boolean ativo
) {}
