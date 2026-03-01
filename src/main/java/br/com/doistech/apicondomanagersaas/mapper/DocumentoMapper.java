package br.com.doistech.apicondomanagersaas.mapper;

import br.com.doistech.apicondomanagersaas.domain.documentosCondominio.DocumentoCondominio;
import br.com.doistech.apicondomanagersaas.dto.documento.DocumentoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface DocumentoMapper {

    @Mapping(target = "condominioId", source = "condominio.id")
    DocumentoResponse toResponse(DocumentoCondominio entity);
}
