package br.com.doistech.apicondomanagersaas.mapper;

import br.com.doistech.apicondomanagersaas.domain.pet.Pet;
import br.com.doistech.apicondomanagersaas.dto.pet.PetResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PetMapper {

    @Mapping(target = "condominioId", source = "condominio.id")
    @Mapping(target = "unidadeId", source = "unidade.id")
    PetResponse toResponse(Pet entity);
}
