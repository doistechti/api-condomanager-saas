package br.com.doistech.apicondomanagersaas.dto.vinculo;

import br.com.doistech.apicondomanagersaas.domain.vinculo.TipoMoradia;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record VinculoCreateRequest(
        @NotNull Long condominioId,
        @NotNull Long unidadeId,
        @NotNull Long pessoaId,
        boolean isProprietario,
        boolean isMorador,
        TipoMoradia tipoMoradia,
        LocalDate dataInicio,
        LocalDate dataFim
) {
}
