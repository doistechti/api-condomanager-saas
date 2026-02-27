package br.com.doistech.apicondomanagersaas.dto.pessoaunidade;

import br.com.doistech.apicondomanagersaas.domain.pessoaUnidade.MoradorTipo;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PessoaUnidadeUpdateRequest(
        String nome,
        String cpfCnpj,
        String email,
        String telefone,

        @NotNull Boolean ehProprietario,
        @NotNull Boolean ehMorador,

        // ✅ novo (quando ehMorador=true)
        MoradorTipo moradorTipo,

        @NotNull Boolean principal,

        LocalDate dataInicio,
        LocalDate dataFim
) {}