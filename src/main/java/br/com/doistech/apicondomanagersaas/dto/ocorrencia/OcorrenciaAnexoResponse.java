package br.com.doistech.apicondomanagersaas.dto.ocorrencia;

import br.com.doistech.apicondomanagersaas.domain.ocorrencia.OcorrenciaArquivoTipo;

import java.time.LocalDateTime;

public record OcorrenciaAnexoResponse(
        Long id,
        String arquivoUrl,
        String arquivoNome,
        String contentType,
        OcorrenciaArquivoTipo tipoArquivo,
        Long tamanhoBytes,
        LocalDateTime createdAt
) {
}
