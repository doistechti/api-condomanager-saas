package br.com.doistech.apicondomanagersaas.mapper;

import br.com.doistech.apicondomanagersaas.domain.pessoaUnidade.PessoaUnidade;
import br.com.doistech.apicondomanagersaas.dto.pessoaunidade.PessoaUnidadeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PessoaUnidadeMapper {

    @Mapping(target = "condominioId", source = "condominio.id")
    @Mapping(target = "unidadeId", source = "unidade.id")
    @Mapping(target = "pessoaId", source = "pessoa.id")
    @Mapping(target = "nome", source = "pessoa.nome")
    @Mapping(target = "cpfCnpj", source = "pessoa.cpfCnpj")
    @Mapping(target = "email", source = "pessoa.email")
    @Mapping(target = "telefone", source = "pessoa.telefone")

    @Mapping(target = "usuarioId", expression = "java(entity.getUsuario() != null ? entity.getUsuario().getId() : null)")
    @Mapping(target = "conviteEnviadoEm", source = "conviteEnviadoEm")
    @Mapping(target = "conviteAceitoEm", source = "conviteAceitoEm")
    @Mapping(target = "moradorTipo", source = "moradorTipo")
    PessoaUnidadeResponse toResponse(PessoaUnidade entity);
}