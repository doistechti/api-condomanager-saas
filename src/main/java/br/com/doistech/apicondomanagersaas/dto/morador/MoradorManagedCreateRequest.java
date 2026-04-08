package br.com.doistech.apicondomanagersaas.dto.morador;

import br.com.doistech.apicondomanagersaas.domain.pessoaUnidade.MoradorTipo;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record MoradorManagedCreateRequest(
        @NotNull
        @JsonAlias({"unidade_id"})
        Long unidadeId,
        String nome,
        @JsonAlias({"cpf", "cpf_cnpj", "documento"})
        String cpfCnpj,
        String email,
        String telefone,
        String fotoUrl,
        String fotoNome,
        MoradorTipo moradorTipo,
        @JsonAlias({"data_inicio"})
        LocalDate dataInicio,
        @JsonAlias({"data_fim"})
        LocalDate dataFim
) {
}
