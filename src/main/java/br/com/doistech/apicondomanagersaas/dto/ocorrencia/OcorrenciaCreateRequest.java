package br.com.doistech.apicondomanagersaas.dto.ocorrencia;

import br.com.doistech.apicondomanagersaas.domain.ocorrencia.OcorrenciaCategoria;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OcorrenciaCreateRequest(
        @NotNull OcorrenciaCategoria categoria,
        @NotBlank String titulo,
        @NotBlank String descricao,
        String localOcorrencia,
        @Valid List<OcorrenciaAnexoCreateRequest> anexos
) {
}
