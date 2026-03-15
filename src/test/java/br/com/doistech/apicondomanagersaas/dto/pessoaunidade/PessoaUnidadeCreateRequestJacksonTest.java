package br.com.doistech.apicondomanagersaas.dto.pessoaunidade;

import br.com.doistech.apicondomanagersaas.domain.pessoaUnidade.MoradorTipo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PessoaUnidadeCreateRequestJacksonTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void shouldDeserializeMoradorPayloadUsingFrontendFormats() throws Exception {
        String json = """
                {
                  "unidadeId": 12,
                  "nome": "Maria Souza",
                  "cpf": "12345678900",
                  "email": "maria@teste.com",
                  "telefone": "11999999999",
                  "tipoMoradia": "inquilino",
                  "ehProprietario": false,
                  "ehMorador": true,
                  "principal": false
                }
                """;

        PessoaUnidadeCreateRequest request = objectMapper.readValue(json, PessoaUnidadeCreateRequest.class);

        assertEquals(12L, request.unidadeId());
        assertEquals("12345678900", request.cpfCnpj());
        assertEquals(MoradorTipo.INQUILINO, request.moradorTipo());
        assertFalse(request.principal());
    }
}
