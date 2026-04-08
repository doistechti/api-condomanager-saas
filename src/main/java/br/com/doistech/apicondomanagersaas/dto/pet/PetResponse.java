package br.com.doistech.apicondomanagersaas.dto.pet;

import java.time.LocalDate;

public record PetResponse(
        Long id,
        Long condominioId,
        Long unidadeId,
        String nome,
        String tipo,
        String raca,
        String porte,
        String cor,
        LocalDate dataNascimento,
        String observacoes,
        String fotoUrl,
        String fotoNome
) {
}
