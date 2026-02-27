package br.com.doistech.apicondomanagersaas.repository;

import br.com.doistech.apicondomanagersaas.domain.reserva.Reserva;
import br.com.doistech.apicondomanagersaas.domain.reserva.ReservaStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findAllByCondominioId(Long condominioId);
    Optional<Reserva> findByIdAndCondominioId(Long id, Long condominioId);

    List<Reserva> findAllByEspacoIdAndCondominioIdAndDataReserva(Long espacoId, Long condominioId, LocalDate dataReserva);
    List<Reserva> findAllByVinculoIdAndCondominioId(Long vinculoId, Long condominioId);
    List<Reserva> findAllByCondominioIdAndStatus(Long condominioId, ReservaStatus status);

    long countByCondominioIdAndStatus(Long condominioId, ReservaStatus status);
}
