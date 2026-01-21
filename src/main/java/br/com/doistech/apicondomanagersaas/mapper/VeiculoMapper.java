package br.com.doistech.apicondomanagersaas.mapper;

import br.com.doistech.apicondomanagersaas.domain.veiculo.Veiculo;
import br.com.doistech.apicondomanagersaas.dto.veiculo.VeiculoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MapperConfig;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VeiculoMapper {

    @Mapping(target = "condominioId", source = "condominio.id")
    @Mapping(target = "pessoaId", source = "pessoa.id")
    VeiculoResponse toResponse(Veiculo entity);
}
