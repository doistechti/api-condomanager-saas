package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.common.exception.BadRequestException;
import br.com.doistech.apicondomanagersaas.common.exception.NotFoundException;
import br.com.doistech.apicondomanagersaas.domain.vinculo.VinculoUnidade;
import br.com.doistech.apicondomanagersaas.dto.vinculo.VinculoCreateRequest;
import br.com.doistech.apicondomanagersaas.dto.vinculo.VinculoUpdateRequest;
import br.com.doistech.apicondomanagersaas.dto.vinculo.VinculoResponse;
import br.com.doistech.apicondomanagersaas.mapper.VinculoUnidadeMapper;
import br.com.doistech.apicondomanagersaas.repository.VinculoUnidadeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VinculoUnidadeService {

    private final VinculoUnidadeRepository repository;
    private final CondominioService condominioService;
    private final UnidadeService unidadeService;
    private final PessoaService pessoaService;
    private final VinculoUnidadeMapper mapper;

    public VinculoResponse create(VinculoCreateRequest req) {
        var condominio = condominioService.getEntity(req.condominioId());
        var unidade = unidadeService.getEntity(req.unidadeId(), req.condominioId());
        var pessoa = pessoaService.getEntity(req.pessoaId(), req.condominioId());

        if (!req.isMorador() && !req.isProprietario()) {
            throw new BadRequestException("Vínculo precisa ser morador e/ou proprietário");
        }

        VinculoUnidade entity = VinculoUnidade.builder()
                .condominio(condominio)
                .unidade(unidade)
                .pessoa(pessoa)
                .isMorador(req.isMorador())
                .isProprietario(req.isProprietario())
                .tipoMoradia(req.tipoMoradia())
                .dataInicio(req.dataInicio())
                .dataFim(req.dataFim())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return mapper.toResponse(repository.save(entity));
    }

    public VinculoResponse update(Long id, Long condominioId, VinculoUpdateRequest req) {
        VinculoUnidade entity = getEntity(id, condominioId);

        if (!req.isMorador() && !req.isProprietario()) {
            throw new BadRequestException("Vínculo precisa ser morador e/ou proprietário");
        }

        entity.setMorador(req.isMorador());
        entity.setProprietario(req.isProprietario());
        entity.setTipoMoradia(req.tipoMoradia());
        entity.setDataInicio(req.dataInicio());
        entity.setDataFim(req.dataFim());
        entity.setUpdatedAt(LocalDateTime.now());

        return mapper.toResponse(repository.save(entity));
    }

    public VinculoResponse getById(Long id, Long condominioId) {
        return mapper.toResponse(getEntity(id, condominioId));
    }

    public List<VinculoResponse> listByCondominio(Long condominioId) {
        return repository.findAllByCondominioId(condominioId).stream().map(mapper::toResponse).toList();
    }

    public List<VinculoResponse> listByUnidade(Long condominioId, Long unidadeId) {
        return repository.findAllByUnidadeIdAndCondominioId(unidadeId, condominioId).stream().map(mapper::toResponse).toList();
    }

    public void delete(Long id, Long condominioId) {
        repository.delete(getEntity(id, condominioId));
    }

    public VinculoUnidade getEntity(Long id, Long condominioId) {
        return repository.findByIdAndCondominioId(id, condominioId)
                .orElseThrow(() -> new NotFoundException("Vínculo não encontrado: " + id));
    }
}
