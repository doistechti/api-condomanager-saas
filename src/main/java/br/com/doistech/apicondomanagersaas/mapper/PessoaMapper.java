package br.com.doistech.apicondomanagersaas.mapper;

import br.com.doistech.apicondomanagersaas.domain.pessoa.Pessoa;
import br.com.doistech.apicondomanagersaas.dto.pessoa.PessoaResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface PessoaMapper {

    @Mapping(target = "condominioId", source = "condominio.id")
    PessoaResponse toResponse(Pessoa entity);
}
