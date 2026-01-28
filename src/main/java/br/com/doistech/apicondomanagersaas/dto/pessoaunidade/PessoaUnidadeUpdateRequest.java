package br.com.doistech.apicondomanagersaas.dto.pessoaunidade;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PessoaUnidadeUpdateRequest(
        // dados pessoais opcionais para atualização
        String nome,
        String cpfCnpj,
        String email,
        String telefone,

        @NotNull Boolean ehProprietario,
        @NotNull Boolean ehMorador,
        @NotNull Boolean principal,

        LocalDate dataInicio,
        LocalDate dataFim
) {}

