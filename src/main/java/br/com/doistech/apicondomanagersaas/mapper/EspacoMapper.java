package br.com.doistech.apicondomanagersaas.mapper;

import br.com.doistech.apicondomanagersaas.domain.espaco.Espaco;
import br.com.doistech.apicondomanagersaas.dto.espaco.EspacoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface EspacoMapper {

    @Mapping(target = "condominioId", source = "condominio.id")
    EspacoResponse toResponse(Espaco entity);
}
