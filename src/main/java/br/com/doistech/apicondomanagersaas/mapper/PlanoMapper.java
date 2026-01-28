package br.com.doistech.apicondomanagersaas.mapper;

import br.com.doistech.apicondomanagersaas.domain.plano.Plano;
import br.com.doistech.apicondomanagersaas.dto.plano.PlanoCreateRequest;
import br.com.doistech.apicondomanagersaas.dto.plano.PlanoResponse;
import br.com.doistech.apicondomanagersaas.dto.plano.PlanoUpdateRequest;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PlanoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "maxAdmins", source = "maxAdmins", qualifiedByName = "toDbMaxAdmins")
    Plano toEntity(PlanoCreateRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "maxAdmins", source = "maxAdmins", qualifiedByName = "toDbMaxAdmins")
    void updateEntity(PlanoUpdateRequest req, @MappingTarget Plano entity);

    @Mapping(target = "maxAdmins", source = "maxAdmins", qualifiedByName = "toApiMaxAdmins")
    PlanoResponse toResponse(Plano entity);

    /**
     * Front manda 999 = ilimitado.
     * Banco salva -1 = ilimitado.
     */
    @Named("toDbMaxAdmins")
    default Integer toDbMaxAdmins(Integer apiValue) {
        if (apiValue == null) return null;
        return apiValue == 999 ? -1 : apiValue;
    }

    /**
     * Banco salva -1 = ilimitado.
     * API devolve 999 = ilimitado (para o front).
     */
    @Named("toApiMaxAdmins")
    default Integer toApiMaxAdmins(Integer dbValue) {
        if (dbValue == null) return null;
        return dbValue == -1 ? 999 : dbValue;
    }
}
