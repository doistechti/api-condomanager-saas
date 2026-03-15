package br.com.doistech.apicondomanagersaas.dto.pessoaunidade;

import br.com.doistech.apicondomanagersaas.domain.pessoaUnidade.MoradorTipo;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PessoaUnidadeCreateRequest(
        @NotNull Long condominioId,
        @NotNull
        @JsonAlias({"unidade_id"})
        Long unidadeId,

        @JsonAlias({"pessoa_id"})
        Long pessoaId,

        String nome,
        @JsonAlias({"cpf", "cpf_cnpj", "documento"})
        String cpfCnpj,
        String email,
        String telefone,

        @NotNull
        @JsonAlias({"isProprietario", "proprietario"})
        Boolean ehProprietario,
        @NotNull
        @JsonAlias({"isMorador", "morador"})
        Boolean ehMorador,

        @JsonAlias({"tipoMoradia", "tipo_moradia"})
        MoradorTipo moradorTipo,

        @NotNull Boolean principal,

        @JsonAlias({"data_inicio"})
        LocalDate dataInicio,
        @JsonAlias({"data_fim"})
        LocalDate dataFim
) {}
