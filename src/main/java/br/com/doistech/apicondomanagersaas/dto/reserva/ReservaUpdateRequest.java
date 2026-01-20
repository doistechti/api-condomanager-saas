package br.com.doistech.apicondomanagersaas.dto.reserva;

import br.com.doistech.apicondomanagersaas.domain.reserva.ReservaStatus;

public record ReservaUpdateRequest(
        ReservaStatus status,
        String motivoRecusa,
        String observacoes
) {
}
