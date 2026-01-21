package br.com.doistech.apicondomanagersaas.mapper;

import br.com.doistech.apicondomanagersaas.domain.condominio.Condominio;
import br.com.doistech.apicondomanagersaas.dto.condominio.CondominioCreateRequest;
import br.com.doistech.apicondomanagersaas.dto.condominio.CondominioResponse;
import br.com.doistech.apicondomanagersaas.dto.condominio.CondominioUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CondominioMapper {

    Condominio toEntity(CondominioCreateRequest request);

    CondominioResponse toResponse(Condominio entity);

    void updateEntity(CondominioUpdateRequest request, @MappingTarget Condominio entity);
}
