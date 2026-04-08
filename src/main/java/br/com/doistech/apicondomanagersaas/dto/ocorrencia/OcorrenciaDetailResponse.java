package br.com.doistech.apicondomanagersaas.dto.ocorrencia;

import br.com.doistech.apicondomanagersaas.domain.ocorrencia.OcorrenciaCategoria;
import br.com.doistech.apicondomanagersaas.domain.ocorrencia.OcorrenciaStatus;

import java.time.LocalDateTime;
import java.util.List;

public record OcorrenciaDetailResponse(
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
        List<OcorrenciaAnexoResponse> anexos,
        List<OcorrenciaMensagemResponse> mensagens,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime resolvidaEm
) {
}
