package br.com.doistech.apicondomanagersaas.dto.chat;

import br.com.doistech.apicondomanagersaas.domain.chat.ConversaTipo;
import jakarta.validation.constraints.NotNull;

public record ConversaCreateRequest(
        @NotNull ConversaTipo tipo,
        Long moradorId,
        String titulo
) {}