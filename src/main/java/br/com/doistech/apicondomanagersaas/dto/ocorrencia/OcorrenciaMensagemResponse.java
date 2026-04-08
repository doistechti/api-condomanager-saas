package br.com.doistech.apicondomanagersaas.dto.ocorrencia;

import br.com.doistech.apicondomanagersaas.domain.ocorrencia.OcorrenciaAutorTipo;

import java.time.LocalDateTime;

public record OcorrenciaMensagemResponse(
        Long id,
        Long autorId,
        OcorrenciaAutorTipo autorTipo,
        String autorNome,
        String mensagem,
        LocalDateTime createdAt
) {
}
