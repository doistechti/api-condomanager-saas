package br.com.doistech.apicondomanagersaas.dto.comunicado;

public record UploadImagemResponse(
        String url,
        String fileName,
        String contentType,
        long size
) {}