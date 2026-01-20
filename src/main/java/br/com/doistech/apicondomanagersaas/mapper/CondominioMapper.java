package br.com.doistech.apicondomanagersaas.mapper;

import br.com.doistech.apicondomanagersaas.domain.condominio.Condominio;
import br.com.doistech.apicondomanagersaas.dto.condominio.CondominioResponse;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface CondominioMapper {
    CondominioResponse toResponse(Condominio entity);
}
