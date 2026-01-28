package br.com.doistech.apicondomanagersaas.dto.pessoaunidade;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PessoaUnidadeCreateRequest(
        @NotNull Long condominioId,
        @NotNull Long unidadeId,

        // Se a pessoa já existir, você pode mandar pessoaId.
        Long pessoaId,

        // Se não existir, criaremos/acharemos por cpfCnpj.
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

