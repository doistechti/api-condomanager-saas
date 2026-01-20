package br.com.doistech.apicondomanagersaas.dto.reserva;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservaCreateRequest(
        @NotNull Long condominioId,
        @NotNull Long espacoId,
        @NotNull Long vinculoId,
        @NotNull LocalDate dataReserva,
        LocalTime horaInicio,
        LocalTime horaFim,
        String observacoes
) {
}
