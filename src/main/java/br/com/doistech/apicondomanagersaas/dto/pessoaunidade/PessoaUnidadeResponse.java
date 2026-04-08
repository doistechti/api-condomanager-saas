package br.com.doistech.apicondomanagersaas.dto.pessoaunidade;

import br.com.doistech.apicondomanagersaas.domain.pessoaUnidade.MoradorTipo;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PessoaUnidadeResponse(
        Long id,
        Long condominioId,
        Long unidadeId,

        Long pessoaId,
        String nome,
        String cpfCnpj,
        String email,
        String telefone,
        String fotoUrl,
        String fotoNome,

        Boolean ehProprietario,
        Boolean ehMorador,
        MoradorTipo moradorTipo,
        Boolean principal,

        LocalDate dataInicio,
        LocalDate dataFim,

        Long usuarioId,
        LocalDateTime conviteEnviadoEm,
        LocalDateTime conviteAceitoEm
) {
    @JsonProperty("tipoMoradia")
    public String tipoMoradiaCompat() {
        return moradorTipo != null ? moradorTipo.name() : null;
    }

    @JsonProperty("tipo")
    public String tipoCompat() {
        return moradorTipo != null ? moradorTipo.name() : null;
    }
}
