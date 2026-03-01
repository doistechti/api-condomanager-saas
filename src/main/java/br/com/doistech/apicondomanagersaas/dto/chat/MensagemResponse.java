package br.com.doistech.apicondomanagersaas.dto.chat;

import br.com.doistech.apicondomanagersaas.domain.chat.RemetenteTipo;

import java.time.LocalDateTime;

public record MensagemResponse(
        Long id,
        Long conversaId,
        Long remetenteId,
        RemetenteTipo remetenteTipo,
        String conteudo,
        boolean lida,
        LocalDateTime createdAt
) {}