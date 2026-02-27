package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.common.exception.BadRequestException;
import br.com.doistech.apicondomanagersaas.common.exception.NotFoundException;
import br.com.doistech.apicondomanagersaas.domain.reserva.Reserva;
import br.com.doistech.apicondomanagersaas.domain.reserva.ReservaStatus;
import br.com.doistech.apicondomanagersaas.dto.reserva.ReservaCreateRequest;
import br.com.doistech.apicondomanagersaas.dto.reserva.ReservaUpdateRequest;
import br.com.doistech.apicondomanagersaas.dto.reserva.ReservaResponse;
import br.com.doistech.apicondomanagersaas.mapper.ReservaMapper;
import br.com.doistech.apicondomanagersaas.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservaService {

    private final ReservaRepository repository;
    private final CondominioService condominioService;
    private final EspacoService espacoService;
    private final VinculoUnidadeService vinculoService;
    private final ReservaMapper mapper;

    public ReservaResponse create(ReservaCreateRequest req) {
        var espaco = espacoService.getEntity(req.espacoId(), req.condominioId());
        var vinculo = vinculoService.getEntity(req.vinculoId(), req.condominioId());

        // Regras simples do MVP: só morador pode reservar
        if (!vinculo.isMorador()) {
            throw new BadRequestException("Apenas moradores podem reservar");
        }

        // Checagem básica de conflito por data (e hora quando existir)
        validateConflito(req.condominioId(), req.espacoId(), req.dataReserva(), req.horaInicio(), req.horaFim());

        ReservaStatus status = espaco.isNecessitaAprovacao() ? ReservaStatus.PENDENTE : ReservaStatus.APROVADA;

        Reserva entity = Reserva.builder()
                .condominio(condominioService.getEntity(req.condominioId()))
                .espaco(espaco)
                .vinculo(vinculo)
                .dataReserva(req.dataReserva())
                .horaInicio(req.horaInicio())
                .horaFim(req.horaFim())
                .observacoes(req.observacoes())
                .status(status)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return mapper.toResponse(repository.save(entity));
    }

    public ReservaResponse updateStatus(Long id, Long condominioId, ReservaUpdateRequest req) {
        Reserva entity = getEntity(id, condominioId);

        if (req.status() != null) {
            entity.setStatus(req.status());
        }
        entity.setMotivoRecusa(req.motivoRecusa());
        entity.setObservacoes(req.observacoes());
        entity.setUpdatedAt(LocalDateTime.now());

        return mapper.toResponse(repository.save(entity));
    }

    public ReservaResponse getById(Long id, Long condominioId) {
        return mapper.toResponse(getEntity(id, condominioId));
    }

    public List<ReservaResponse> listByCondominio(Long condominioId) {
        return repository.findAllByCondominioId(condominioId).stream().map(mapper::toResponse).toList();
    }

    public List<ReservaResponse> listPendentes(Long condominioId) {
        return repository.findAllByCondominioIdAndStatus(condominioId, ReservaStatus.PENDENTE)
                .stream().map(mapper::toResponse).toList();
    }

    public void delete(Long id, Long condominioId) {
        repository.delete(getEntity(id, condominioId));
    }

    private Reserva getEntity(Long id, Long condominioId) {
        return repository.findByIdAndCondominioId(id, condominioId)
                .orElseThrow(() -> new NotFoundException("Reserva não encontrada: " + id));
    }

    private void validateConflito(Long condominioId, Long espacoId, java.time.LocalDate data, LocalTime inicio, LocalTime fim) {
        List<Reserva> doDia = repository.findAllByEspacoIdAndCondominioIdAndDataReserva(espacoId, condominioId, data);

        // Se não tiver hora, tratamos como reserva do dia todo
        LocalTime start = inicio != null ? inicio : LocalTime.MIN;
        LocalTime end = fim != null ? fim : LocalTime.MAX;

        for (Reserva r : doDia) {
            if (r.getStatus() == ReservaStatus.RECUSADA || r.getStatus() == ReservaStatus.CANCELADA) {
                continue;
            }
            LocalTime rStart = r.getHoraInicio() != null ? r.getHoraInicio() : LocalTime.MIN;
            LocalTime rEnd = r.getHoraFim() != null ? r.getHoraFim() : LocalTime.MAX;

            boolean overlap = start.isBefore(rEnd) && end.isAfter(rStart);
            if (overlap) {
                throw new BadRequestException("Conflito de reserva para este espaço/data/horário");
            }
        }
    }

    public List<ReservaResponse> listFiltered(
            Long condominioId,
            Long espacoId,
            LocalDate dataInicio,
            LocalDate dataFim,
            ReservaStatus status
    ) {
        return repository.findAllByCondominioId(condominioId)
                .stream()
                .filter(r -> espacoId == null || r.getEspaco().getId().equals(espacoId))
                .filter(r -> status == null || r.getStatus() == status)
                .filter(r -> {
                    if (dataInicio == null && dataFim == null) return true;
                    LocalDate d = r.getDataReserva();
                    if (d == null) return false;
                    if (dataInicio != null && d.isBefore(dataInicio)) return false;
                    if (dataFim != null && d.isAfter(dataFim)) return false;
                    return true;
                })
                .map(mapper::toResponse)
                .toList();
    }
}
