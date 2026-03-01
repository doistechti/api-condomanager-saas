package br.com.doistech.apicondomanagersaas.dto.morador;

/**
 * Representa um vínculo (PessoaUnidade) do morador.
 *
 * Importante: para o portal MORADOR, o "id" aqui é o id do vínculo (pessoa_unidade).
 * Esse id é o que o backend usa em reservas (reserva.vinculo_id), etc.
 */
public record MoradorVinculoResponse(
        Long id,
        Long pessoaId,
        Long unidadeId,
        Boolean principal,
        String moradorTipo
) {
}