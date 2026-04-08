package br.com.doistech.apicondomanagersaas.dto.ocorrencia;

import br.com.doistech.apicondomanagersaas.domain.ocorrencia.OcorrenciaCategoria;
import br.com.doistech.apicondomanagersaas.domain.ocorrencia.OcorrenciaStatus;

import java.time.LocalDateTime;

public record OcorrenciaSummaryResponse(
        Long id,
        String codigo,
        Long condominioId,
        Long moradorId,
        Long unidadeId,
        OcorrenciaCategoria categoria,
        String titulo,
        String descricao,
        String localOcorrencia,
        OcorrenciaStatus status,
        String moradorNome,
        String unidadeIdentificacao,
        int anexosCount,
        int mensagensCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime resolvidaEm
) {
}
