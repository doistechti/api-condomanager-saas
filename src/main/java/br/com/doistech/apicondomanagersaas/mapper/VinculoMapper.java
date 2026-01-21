package br.com.doistech.apicondomanagersaas.mapper;

import br.com.doistech.apicondomanagersaas.domain.vinculo.VinculoUnidade;
import br.com.doistech.apicondomanagersaas.dto.vinculo.VinculoResponse;
import br.com.doistech.apicondomanagersaas.dto.vinculo.VinculoUpdateRequest;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface VinculoMapper {

    @Mapping(target = "condominioId", source = "condominio.id")
    @Mapping(target = "pessoaId", source = "pessoa.id")
    @Mapping(target = "unidadeId", source = "unidade.id")
    VinculoResponse toResponse(VinculoUnidade entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(VinculoUpdateRequest dto, @MappingTarget VinculoUnidade entity);
}
