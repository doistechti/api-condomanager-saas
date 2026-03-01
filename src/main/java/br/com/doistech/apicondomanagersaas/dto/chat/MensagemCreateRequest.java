package br.com.doistech.apicondomanagersaas.dto.chat;

import jakarta.validation.constraints.NotBlank;

public record MensagemCreateRequest(
        @NotBlank String conteudo
) {}