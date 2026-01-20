package br.com.doistech.apicondomanagersaas.dto.vinculo;

import br.com.doistech.apicondomanagersaas.domain.vinculo.TipoMoradia;

import java.time.LocalDate;

public record VinculoResponse(
        Long id,
        Long condominioId,
        Long unidadeId,
        Long pessoaId,
        boolean isProprietario,
        boolean isMorador,
        TipoMoradia tipoMoradia,
        LocalDate dataInicio,
        LocalDate dataFim
) {
}
