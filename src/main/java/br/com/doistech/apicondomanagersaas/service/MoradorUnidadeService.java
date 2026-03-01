package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.dto.morador.MoradorUnidadeResponse;
import br.com.doistech.apicondomanagersaas.mapper.UnidadeMapper;
import br.com.doistech.apicondomanagersaas.repository.UnidadeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MoradorUnidadeService {

    private final MoradorScopeService moradorScopeService;
    private final UnidadeRepository unidadeRepository;
    private final UnidadeMapper unidadeMapper;

    public List<MoradorUnidadeResponse> listarMinhasUnidades(String email) {
        var scope = moradorScopeService.getScope(email);

        if (scope.unidadeIds() == null || scope.unidadeIds().isEmpty()) {
            return List.of();
        }

        return unidadeRepository.findAllByIdInAndCondominioId(scope.unidadeIds(), scope.condominioId())
                .stream()
                .filter(u -> Boolean.TRUE.equals(u.getAtivo()))
                // ✅ aqui estava o erro: toResponse() devolve UnidadeResponse
                .map(unidadeMapper::toMoradorResponse)
                .toList();
    }
}