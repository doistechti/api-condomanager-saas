package br.com.doistech.apicondomanagersaas.mapper;

import br.com.doistech.apicondomanagersaas.domain.vinculo.VinculoUnidade;
import br.com.doistech.apicondomanagersaas.dto.vinculo.VinculoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MapperConfig;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VinculoUnidadeMapper {

    @Mapping(target = "condominioId", source = "condominio.id")
    @Mapping(target = "unidadeId", source = "unidade.id")
    @Mapping(target = "pessoaId", source = "pessoa.id")
    VinculoResponse toResponse(VinculoUnidade entity);
}
