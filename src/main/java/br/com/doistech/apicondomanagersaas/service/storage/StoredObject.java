package br.com.doistech.apicondomanagersaas.service.storage;

public record StoredObject(
        String key,
        String url,
        String fileName,
        String contentType,
        long size
) {
}
