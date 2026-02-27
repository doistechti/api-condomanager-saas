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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PessoaUnidadeService {

    private final PessoaUnidadeRepository repository;
    private final PessoaRepository pessoaRepository;
    private final CondominioService condominioService;
    private final UnidadeRepository unidadeRepository;
    private final PessoaUnidadeMapper mapper;

    public PessoaUnidadeResponse create(PessoaUnidadeCreateRequest req) {
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

        // ✅ regra: se já existe vínculo ativo para (unidade,pessoa) atualiza flags ao invés de duplicar
        // (isso é essencial para "proprietário que também mora")
        var existenteOpt = repository.findAllByCondominioIdAndUnidadeIdAndAtivoTrue(req.condominioId(), req.unidadeId())
                .stream()
                .filter(v -> v.getPessoa().getId().equals(pessoa.getId()))
                .findFirst();

        PessoaUnidade pu = existenteOpt.orElseGet(PessoaUnidade::new);

        pu.setCondominio(condominio);
        pu.setUnidade(unidade);
        pu.setPessoa(pessoa);

        pu.setEhProprietario(req.ehProprietario());
        pu.setEhMorador(req.ehMorador());

        // ✅ guarda o tipo quando for morador
        if (Boolean.TRUE.equals(req.ehMorador())) {
            pu.setMoradorTipo(req.moradorTipo());
        } else {
            pu.setMoradorTipo(null);
        }

        pu.setPrincipal(req.principal());
        pu.setDataInicio(req.dataInicio());
        pu.setDataFim(req.dataFim());

        pu.setAtivo(true);

        if (pu.getCreatedAt() == null) pu.setCreatedAt(LocalDateTime.now());
        pu.setUpdatedAt(LocalDateTime.now());

        repository.save(pu);
        return mapper.toResponse(pu);
    }

    public PessoaUnidadeResponse update(Long id, Long condominioId, PessoaUnidadeUpdateRequest req) {
        PessoaUnidade pu = getEntity(id, condominioId);

        if (Boolean.TRUE.equals(req.principal())) {
            boolean jaExisteOutro = repository.existsByCondominioIdAndUnidadeIdAndPrincipalTrueAndAtivoTrueAndIdNot(
                    condominioId, pu.getUnidade().getId(), id
            );
            if (jaExisteOutro) {
                throw new BadRequestException("Já existe um responsável principal nesta unidade.");
            }
        }

        Pessoa p = pu.getPessoa();
        if (req.nome() != null) p.setNome(req.nome());
        if (req.cpfCnpj() != null) p.setCpfCnpj(req.cpfCnpj());
        if (req.email() != null) p.setEmail(req.email());
        if (req.telefone() != null) p.setTelefone(req.telefone());
        p.setUpdatedAt(LocalDateTime.now());
        pessoaRepository.save(p);

        pu.setEhProprietario(req.ehProprietario());
        pu.setEhMorador(req.ehMorador());

        if (Boolean.TRUE.equals(req.ehMorador())) {
            pu.setMoradorTipo(req.moradorTipo());
        } else {
            pu.setMoradorTipo(null);
        }

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

    // ✅ fachadas
    public List<PessoaUnidadeResponse> listMoradores(Long condominioId) {
        return repository.findAllByCondominioIdAndEhMoradorTrueAndAtivoTrue(condominioId)
                .stream().map(mapper::toResponse).toList();
    }

    public List<PessoaUnidadeResponse> listProprietarios(Long condominioId) {
        return repository.findAllByCondominioIdAndEhProprietarioTrueAndAtivoTrue(condominioId)
                .stream().map(mapper::toResponse).toList();
    }

    // ✅ convite (MVP)
    public PessoaUnidadeResponse enviarConvite(Long id, Long condominioId) {
        PessoaUnidade pu = getEntity(id, condominioId);

        if (!Boolean.TRUE.equals(pu.getEhMorador())) {
            throw new BadRequestException("Convite só pode ser enviado para moradores.");
        }

        if (pu.getUsuario() != null) {
            throw new BadRequestException("Este morador já possui uma conta ativa.");
        }

        // precisa ter e-mail na pessoa
        if (pu.getPessoa() == null || pu.getPessoa().getEmail() == null || pu.getPessoa().getEmail().isBlank()) {
            throw new BadRequestException("Morador não possui e-mail cadastrado.");
        }

        pu.setConviteToken(UUID.randomUUID().toString());
        pu.setConviteEnviadoEm(LocalDateTime.now());
        pu.setUpdatedAt(LocalDateTime.now());
        repository.save(pu);

        return mapper.toResponse(pu);
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

        if (req.cpfCnpj() != null && !req.cpfCnpj().isBlank()) {
            return pessoaRepository.findByCpfCnpj(req.cpfCnpj())
                    .orElseGet(() -> criarPessoa(req));
        }

        return criarPessoa(req);
    }

    private Pessoa criarPessoa(PessoaUnidadeCreateRequest req) {
        if (req.nome() == null || req.nome().isBlank()) {
            throw new BadRequestException("Nome é obrigatório para criar uma pessoa.");
        }

        // ✅ garante condominio_id preenchido (coluna NOT NULL)
        Condominio condominio = condominioService.getEntity(req.condominioId());

        Pessoa p = new Pessoa();
        p.setCondominio(condominio); // ✅ ESSENCIAL

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