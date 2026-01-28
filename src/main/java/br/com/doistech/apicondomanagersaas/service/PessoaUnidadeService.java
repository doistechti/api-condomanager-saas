package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.common.exception.BadRequestException;
import br.com.doistech.apicondomanagersaas.common.exception.NotFoundException;
import br.com.doistech.apicondomanagersaas.domain.condominio.Condominio;
import br.com.doistech.apicondomanagersaas.domain.pessoa.Pessoa;
import br.com.doistech.apicondomanagersaas.domain.pessoaUnidade.PessoaUnidade;
import br.com.doistech.apicondomanagersaas.dto.pessoaunidade.PessoaUnidadeCreateRequest;
import br.com.doistech.apicondomanagersaas.dto.pessoaunidade.PessoaUnidadeResponse;
import br.com.doistech.apicondomanagersaas.dto.pessoaunidade.PessoaUnidadeUpdateRequest;
import br.com.doistech.apicondomanagersaas.mapper.PessoaUnidadeMapper;
import br.com.doistech.apicondomanagersaas.repository.PessoaRepository;
import br.com.doistech.apicondomanagersaas.repository.PessoaUnidadeRepository;
import br.com.doistech.apicondomanagersaas.repository.UnidadeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PessoaUnidadeService {

    private final PessoaUnidadeRepository repository;
    private final PessoaRepository pessoaRepository;
    private final CondominioService condominioService;
    private final UnidadeRepository unidadeRepository;
    private final PessoaUnidadeMapper mapper;

    public PessoaUnidadeResponse create(PessoaUnidadeCreateRequest req) {
        // regra B: se principal=true e já existe outro principal na unidade => bloqueia
        if (Boolean.TRUE.equals(req.principal())) {
            boolean jaExiste = repository.existsByCondominioIdAndUnidadeIdAndPrincipalTrueAndAtivoTrue(
                    req.condominioId(), req.unidadeId()
            );
            if (jaExiste) {
                throw new BadRequestException("Já existe um responsável principal nesta unidade.");
            }
        }

        Condominio condominio = condominioService.getEntity(req.condominioId());

        var unidade = unidadeRepository.findByIdAndCondominioId(req.unidadeId(), req.condominioId())
                .orElseThrow(() -> new NotFoundException("Unidade não encontrada para este condomínio"));

        Pessoa pessoa = resolvePessoa(req);

        PessoaUnidade pu = new PessoaUnidade();
        pu.setCondominio(condominio);
        pu.setUnidade(unidade);
        pu.setPessoa(pessoa);

        pu.setEhProprietario(req.ehProprietario());
        pu.setEhMorador(req.ehMorador());
        pu.setPrincipal(req.principal());

        pu.setDataInicio(req.dataInicio());
        pu.setDataFim(req.dataFim());

        pu.setAtivo(true);
        pu.setCreatedAt(LocalDateTime.now());
        pu.setUpdatedAt(LocalDateTime.now());

        repository.save(pu);
        return mapper.toResponse(pu);
    }

    public PessoaUnidadeResponse update(Long id, Long condominioId, PessoaUnidadeUpdateRequest req) {
        PessoaUnidade pu = getEntity(id, condominioId);

        // regra B: se principal=true e existe outro principal (id diferente) => bloqueia
        if (Boolean.TRUE.equals(req.principal())) {
            boolean jaExisteOutro = repository.existsByCondominioIdAndUnidadeIdAndPrincipalTrueAndAtivoTrueAndIdNot(
                    condominioId, pu.getUnidade().getId(), id
            );
            if (jaExisteOutro) {
                throw new BadRequestException("Já existe um responsável principal nesta unidade.");
            }
        }

        // Atualiza dados pessoais (se vierem)
        Pessoa p = pu.getPessoa();
        if (req.nome() != null) p.setNome(req.nome());
        if (req.cpfCnpj() != null) p.setCpfCnpj(req.cpfCnpj());
        if (req.email() != null) p.setEmail(req.email());
        if (req.telefone() != null) p.setTelefone(req.telefone());
        p.setUpdatedAt(LocalDateTime.now());
        pessoaRepository.save(p);

        pu.setEhProprietario(req.ehProprietario());
        pu.setEhMorador(req.ehMorador());
        pu.setPrincipal(req.principal());
        pu.setDataInicio(req.dataInicio());
        pu.setDataFim(req.dataFim());
        pu.setUpdatedAt(LocalDateTime.now());

        repository.save(pu);
        return mapper.toResponse(pu);
    }

    public PessoaUnidadeResponse getById(Long id, Long condominioId) {
        return mapper.toResponse(getEntity(id, condominioId));
    }

    public List<PessoaUnidadeResponse> listByCondominio(Long condominioId) {
        return repository.findAllByCondominioIdAndAtivoTrue(condominioId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public List<PessoaUnidadeResponse> listByUnidade(Long condominioId, Long unidadeId) {
        return repository.findAllByCondominioIdAndUnidadeIdAndAtivoTrue(condominioId, unidadeId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    // soft delete
    public void delete(Long id, Long condominioId) {
        PessoaUnidade pu = getEntity(id, condominioId);
        pu.setAtivo(false);
        pu.setUpdatedAt(LocalDateTime.now());
        repository.save(pu);
    }

    public PessoaUnidade getEntity(Long id, Long condominioId) {
        return repository.findByIdAndCondominioId(id, condominioId)
                .orElseThrow(() -> new NotFoundException("Vínculo não encontrado para este condomínio"));
    }

    private Pessoa resolvePessoa(PessoaUnidadeCreateRequest req) {
        if (req.pessoaId() != null) {
            return pessoaRepository.findById(req.pessoaId())
                    .orElseThrow(() -> new NotFoundException("Pessoa não encontrada"));
        }

        // se tiver cpfCnpj, tenta reutilizar pessoa existente para evitar duplicidade
        if (req.cpfCnpj() != null && !req.cpfCnpj().isBlank()) {
            return pessoaRepository.findByCpfCnpj(req.cpfCnpj())
                    .orElseGet(() -> criarPessoa(req));
        }

        // sem cpfCnpj, cria novo (você pode endurecer essa regra depois)
        return criarPessoa(req);
    }

    private Pessoa criarPessoa(PessoaUnidadeCreateRequest req) {
        if (req.nome() == null || req.nome().isBlank()) {
            throw new BadRequestException("Nome é obrigatório para criar uma pessoa.");
        }

        Pessoa p = new Pessoa();
        p.setNome(req.nome());
        p.setCpfCnpj(req.cpfCnpj());
        p.setEmail(req.email());
        p.setTelefone(req.telefone());
        p.setAtivo(true);
        p.setCreatedAt(LocalDateTime.now());
        p.setUpdatedAt(LocalDateTime.now());
        return pessoaRepository.save(p);
    }
}

