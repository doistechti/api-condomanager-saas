package br.com.doistech.apicondomanagersaas.mapper;

import br.com.doistech.apicondomanagersaas.domain.comunicado.Comunicado;
import br.com.doistech.apicondomanagersaas.dto.comunicado.ComunicadoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface ComunicadoMapper {

    @Mapping(target = "condominioId", source = "condominio.id")
    ComunicadoResponse toResponse(Comunicado entity);
}