package br.com.doistech.apicondomanagersaas.controller;

import br.com.doistech.apicondomanagersaas.domain.reserva.ReservaStatus;
import br.com.doistech.apicondomanagersaas.dto.reserva.ReservaCreateRequest;
import br.com.doistech.apicondomanagersaas.dto.reserva.ReservaResponse;
import br.com.doistech.apicondomanagersaas.dto.reserva.ReservaUpdateRequest;
import br.com.doistech.apicondomanagersaas.service.ReservaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/condominios/{condominioId}/reservas")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservaResponse create(@PathVariable Long condominioId, @Valid @RequestBody ReservaCreateRequest req) {
        ReservaCreateRequest fixed = new ReservaCreateRequest(
                condominioId,
                req.espacoId(),
                req.vinculoId(),
                req.dataReserva(),
                req.horaInicio(),
                req.horaFim(),
                req.observacoes()
        );
        return service.create(fixed);
    }

    @PatchMapping("/{id}")
    public ReservaResponse updateStatus(
            @PathVariable Long condominioId,
            @PathVariable Long id,
            @RequestBody ReservaUpdateRequest req
    ) {
        return service.updateStatus(id, condominioId, req);
    }

    @GetMapping("/{id}")
    public ReservaResponse getById(@PathVariable Long condominioId, @PathVariable Long id) {
        return service.getById(id, condominioId);
    }

    /**
     * ✅ Agora com filtros opcionais:
     * /reservas?espacoId=&dataInicio=YYYY-MM-DD&dataFim=YYYY-MM-DD&status=
     */
    @GetMapping
    public List<ReservaResponse> list(
            @PathVariable Long condominioId,
            @RequestParam(required = false) Long espacoId,
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim,
            @RequestParam(required = false) ReservaStatus status
    ) {
        return service.listFiltered(condominioId, espacoId, dataInicio, dataFim, status);
    }

    @GetMapping("/pendentes")
    public List<ReservaResponse> listPendentes(@PathVariable Long condominioId) {
        return service.listPendentes(condominioId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long condominioId, @PathVariable Long id) {
        service.delete(id, condominioId);
    }
}