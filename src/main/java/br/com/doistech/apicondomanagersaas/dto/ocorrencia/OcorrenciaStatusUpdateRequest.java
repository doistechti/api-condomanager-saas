package br.com.doistech.apicondomanagersaas.dto.ocorrencia;

import br.com.doistech.apicondomanagersaas.domain.ocorrencia.OcorrenciaStatus;
import jakarta.validation.constraints.NotNull;

public record OcorrenciaStatusUpdateRequest(
        @NotNull OcorrenciaStatus status
) {
}
