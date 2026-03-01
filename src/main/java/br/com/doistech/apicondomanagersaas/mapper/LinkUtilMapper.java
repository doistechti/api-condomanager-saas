package br.com.doistech.apicondomanagersaas.mapper;

import br.com.doistech.apicondomanagersaas.domain.linkutil.LinkUtil;
import br.com.doistech.apicondomanagersaas.dto.linkutil.LinkUtilResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface LinkUtilMapper {

    @Mapping(target = "condominioId", source = "condominio.id")
    LinkUtilResponse toResponse(LinkUtil entity);
}