package br.com.doistech.apicondomanagersaas.mapper;

import br.com.doistech.apicondomanagersaas.domain.setor.Setor;
import br.com.doistech.apicondomanagersaas.dto.setor.SetorResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MapperConfig;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SetorMapper {

    @Mapping(target = "condominioId", source = "condominio.id")
    SetorResponse toResponse(Setor entity);
}
