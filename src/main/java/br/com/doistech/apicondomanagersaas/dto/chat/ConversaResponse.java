package br.com.doistech.apicondomanagersaas.dto.chat;

import br.com.doistech.apicondomanagersaas.domain.chat.ConversaStatus;
import br.com.doistech.apicondomanagersaas.domain.chat.ConversaTipo;

import java.time.LocalDateTime;

public record ConversaResponse(
        Long id,
        ConversaTipo tipo,
        Long condominioId,
        Long moradorId,
        String titulo,
        ConversaStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime ultimaMensagemAt,
        String condominioNome,
        String moradorNome,
        long unreadCount
) {}