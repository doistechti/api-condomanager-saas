package br.com.doistech.apicondomanagersaas.dto.adminsaas;

import java.time.LocalDateTime;

public record RecentCondominioResponse(
        Long id,
        String nome,
        LocalDateTime createdAt,
        String planoNome
) {}

