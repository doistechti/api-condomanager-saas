package br.com.doistech.apicondomanagersaas.mapper;

import br.com.doistech.apicondomanagersaas.domain.setor.Setor;
import br.com.doistech.apicondomanagersaas.dto.setor.SetorResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface SetorMapper {

    @Mapping(target = "condominioId", source = "condominio.id")
    SetorResponse toResponse(Setor entity);
}
