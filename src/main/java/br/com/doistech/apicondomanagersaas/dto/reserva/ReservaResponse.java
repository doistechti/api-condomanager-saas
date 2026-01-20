package br.com.doistech.apicondomanagersaas.dto.reserva;

import br.com.doistech.apicondomanagersaas.domain.reserva.ReservaStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservaResponse(
        Long id,
        Long condominioId,
        Long espacoId,
        Long vinculoId,
        LocalDate dataReserva,
        LocalTime horaInicio,
        LocalTime horaFim,
        ReservaStatus status,
        String motivoRecusa,
        String observacoes
) {
}
