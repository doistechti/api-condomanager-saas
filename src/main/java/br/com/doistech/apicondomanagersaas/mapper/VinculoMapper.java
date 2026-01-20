package br.com.doistech.apicondomanagersaas.mapper;

import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface VinculoMapper {

    @Mapping(target = "condominioId", source = "condominio.id")
    @Mapping(target = "pessoaId", source = "pessoa.id")
    @Mapping(target = "unidadeId", source = "unidade.id")
    VinculoDtos.VinculoResponse toResponse(VinculoUnidade entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(VinculoDtos.VinculoUpdateRequest dto, @MappingTarget VinculoUnidade entity);
}
