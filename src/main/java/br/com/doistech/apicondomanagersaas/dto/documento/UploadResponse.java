package br.com.doistech.apicondomanagersaas.dto.documento;

public record UploadResponse(
        String url,
        String fileName,
        String contentType,
        long size
) {}
