package br.com.doistech.apicondomanagersaas.dto.pessoaunidade;

import br.com.doistech.apicondomanagersaas.domain.pessoaUnidade.MoradorTipo;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PessoaUnidadeUpdateRequest(
        String nome,
        @JsonAlias({"cpf", "cpf_cnpj", "documento"})
        String cpfCnpj,
        String email,
        String telefone,

        @NotNull
        @JsonAlias({"isProprietario", "proprietario", "is_owner", "owner", "titular"})
        Boolean ehProprietario,
        @NotNull
        @JsonAlias({"isMorador", "morador", "resident"})
        Boolean ehMorador,

        @JsonAlias({"tipoMoradia", "tipo_moradia", "tipo", "tipo_morador", "morador_tipo"})
        MoradorTipo moradorTipo,

        @NotNull Boolean principal,

        @JsonAlias({"data_inicio"})
        LocalDate dataInicio,
        @JsonAlias({"data_fim"})
        LocalDate dataFim
) {}
