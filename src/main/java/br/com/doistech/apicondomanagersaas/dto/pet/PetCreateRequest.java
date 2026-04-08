package br.com.doistech.apicondomanagersaas.dto.pet;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PetCreateRequest(
        @NotNull Long condominioId,
        @NotNull Long unidadeId,
        @NotBlank String nome,
        @NotBlank String tipo,
        String raca,
        String porte,
        String cor,
        LocalDate dataNascimento,
        String observacoes,
        String fotoUrl,
        String fotoNome
) {
}
