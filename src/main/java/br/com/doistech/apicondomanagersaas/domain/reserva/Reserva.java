package br.com.doistech.apicondomanagersaas.domain.reserva;

import br.com.doistech.apicondomanagersaas.domain.condominio.Condominio;
import br.com.doistech.apicondomanagersaas.domain.espaco.Espaco;
import br.com.doistech.apicondomanagersaas.domain.vinculo.VinculoUnidade;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "reservas", indexes = {
        @Index(name = "idx_reservas_condominio", columnList = "condominio_id"),
        @Index(name = "idx_reservas_espaco", columnList = "espaco_id"),
        @Index(name = "idx_reservas_data", columnList = "data_reserva")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "condominio_id")
    private Condominio condominio;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "espaco_id")
    private Espaco espaco;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "vinculo_id")
    private VinculoUnidade vinculo;

    @Column(name = "data_reserva", nullable = false)
    private LocalDate dataReserva;

    private LocalTime horaInicio;
    private LocalTime horaFim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservaStatus status;

    private String motivoRecusa;

    @Lob
    private String observacoes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
