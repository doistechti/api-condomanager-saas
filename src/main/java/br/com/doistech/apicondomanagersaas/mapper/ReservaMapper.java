package br.com.doistech.apicondomanagersaas.mapper;

import br.com.doistech.apicondomanagersaas.domain.reserva.Reserva;
import br.com.doistech.apicondomanagersaas.dto.reserva.ReservaResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface ReservaMapper {

    @Mapping(target = "condominioId", source = "condominio.id")
    @Mapping(target = "espacoId", source = "espaco.id")
    @Mapping(target = "vinculoId", source = "vinculo.id")
    ReservaResponse toResponse(Reserva entity);
}
