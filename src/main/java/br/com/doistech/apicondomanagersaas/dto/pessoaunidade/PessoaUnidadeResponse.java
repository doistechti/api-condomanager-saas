package br.com.doistech.apicondomanagersaas.dto.pessoaunidade;

import java.time.LocalDate;

public record PessoaUnidadeResponse(
        Long id,
        Long condominioId,
        Long unidadeId,

        Long pessoaId,
        String nome,
        String cpfCnpj,
        String email,
        String telefone,

        Boolean ehProprietario,
        Boolean ehMorador,
        Boolean principal,

        LocalDate dataInicio,
        LocalDate dataFim,

        Long usuarioId
) {}

