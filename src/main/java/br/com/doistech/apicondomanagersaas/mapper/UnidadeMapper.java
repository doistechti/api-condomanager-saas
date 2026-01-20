package br.com.doistech.apicondomanagersaas.mapper;

import br.com.doistech.apicondomanagersaas.domain.unidade.Unidade;
import br.com.doistech.apicondomanagersaas.dto.unidade.UnidadeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface UnidadeMapper {

    @Mapping(target = "condominioId", source = "condominio.id")
    @Mapping(target = "setorId", expression = "java(entity.getSetor() != null ? entity.getSetor().getId() : null)")
    UnidadeResponse toResponse(Unidade entity);
}
