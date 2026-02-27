package br.com.doistech.apicondomanagersaas.dto.pessoaunidade;

import br.com.doistech.apicondomanagersaas.domain.pessoaUnidade.MoradorTipo;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
        MoradorTipo moradorTipo,
        Boolean principal,

        LocalDate dataInicio,
        LocalDate dataFim,

        Long usuarioId,
        LocalDateTime conviteEnviadoEm,
        LocalDateTime conviteAceitoEm
) {}