package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.common.exception.BadRequestException;
import br.com.doistech.apicondomanagersaas.common.exception.NotFoundException;
import br.com.doistech.apicondomanagersaas.domain.condominio.Condominio;
import br.com.doistech.apicondomanagersaas.domain.pessoa.Pessoa;
import br.com.doistech.apicondomanagersaas.domain.pessoaUnidade.PessoaUnidade;
import br.com.doistech.apicondomanagersaas.domain.role.Role;
import br.com.doistech.apicondomanagersaas.domain.usuario.Usuario;
import br.com.doistech.apicondomanagersaas.dto.pessoaunidade.PessoaUnidadeCreateRequest;
import br.com.doistech.apicondomanagersaas.dto.pessoaunidade.PessoaUnidadeResponse;
import br.com.doistech.apicondomanagersaas.dto.pessoaunidade.PessoaUnidadeUpdateRequest;
import br.com.doistech.apicondomanagersaas.mapper.PessoaUnidadeMapper;
import br.com.doistech.apicondomanagersaas.repository.PessoaRepository;
import br.com.doistech.apicondomanagersaas.repository.PessoaUnidadeRepository;
import br.com.doistech.apicondomanagersaas.repository.RoleRepository;
import br.com.doistech.apicondomanagersaas.repository.UnidadeRepository;
import br.com.doistech.apicondomanagersaas.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class PessoaUnidadeService {

    private static final List<String> TEMP_PASSWORD_WORDS = List.of(
            "rosa", "casa", "vida", "lago", "sola", "ninho", "folha", "porto", "piso", "vento"
    );

    private final PessoaUnidadeRepository repository;
    private final PessoaRepository pessoaRepository;
    private final CondominioService condominioService;
    private final UnidadeRepository unidadeRepository;
    private final PessoaUnidadeMapper mapper;
    private final MoradorInviteEmailService moradorInviteEmailService;
    private final UsuarioRepository usuarioRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public PessoaUnidadeResponse create(PessoaUnidadeCreateRequest req) {
        Condominio condominio = condominioService.getEntity(req.condominioId());

        var unidade = unidadeRepository.findByIdAndCondominioId(req.unidadeId(), req.condominioId())
                .orElseThrow(() -> new NotFoundException("Unidade nao encontrada para este condominio"));

        if (req.pessoaId() == null && normalizeCpf(req.cpfCnpj()) == null) {
            throw new BadRequestException("cpfCnpj e obrigatorio para criar um morador novo.");
        }

        List<PessoaUnidade> vinculosAtivos = repository.findAllByCondominioIdAndUnidadeIdAndAtivoTrue(
                req.condominioId(), req.unidadeId()
        );

        // A regra operacional do cadastro e por cpf+unidade:
        // mesmo cpf na mesma unidade -> atualiza o vinculo existente;
        // cpf diferente na mesma unidade -> cria um novo vinculo;
        // se ja existir principal na unidade, o novo vinculo entra como secundario.
        Optional<PessoaUnidade> vinculoExistenteOpt = findVinculoByCpf(vinculosAtivos, req.cpfCnpj());

        PessoaUnidade pu;
        Pessoa pessoa;
        boolean principal;

        if (vinculoExistenteOpt.isPresent()) {
            pu = vinculoExistenteOpt.get();
            pessoa = pu.getPessoa();
            principal = resolvePrincipalOnUpdate(req.condominioId(), req.unidadeId(), pu.getId(), req.principal());
        } else {
            pessoa = resolvePessoa(req);
            pu = new PessoaUnidade();
            principal = resolvePrincipalOnCreate(vinculosAtivos, req.principal());
        }

        pu.setCondominio(condominio);
        pu.setUnidade(unidade);
        pu.setPessoa(pessoa);
        pu.setEhProprietario(req.ehProprietario());
        pu.setEhMorador(req.ehMorador());

        if (Boolean.TRUE.equals(req.ehMorador())) {
            pu.setMoradorTipo(req.moradorTipo());
        } else {
            pu.setMoradorTipo(null);
        }

        pu.setPrincipal(principal);
        pu.setDataInicio(req.dataInicio());
        pu.setDataFim(req.dataFim());
        pu.setAtivo(true);

        if (pu.getCreatedAt() == null) {
            pu.setCreatedAt(LocalDateTime.now());
        }
        pu.setUpdatedAt(LocalDateTime.now());

        repository.save(pu);
        return mapper.toResponse(pu);
    }

    public PessoaUnidadeResponse update(Long id, Long condominioId, PessoaUnidadeUpdateRequest req) {
        PessoaUnidade pu = getEntity(id, condominioId);
        boolean principal = resolvePrincipalOnUpdate(condominioId, pu.getUnidade().getId(), id, req.principal());

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

        pu.setPrincipal(principal);
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

    public List<PessoaUnidadeResponse> listMoradores(Long condominioId) {
        return repository.findAllByCondominioIdAndEhMoradorTrueAndAtivoTrue(condominioId)
                .stream().map(mapper::toResponse).toList();
    }

    public List<PessoaUnidadeResponse> listProprietarios(Long condominioId) {
        return repository.findAllByCondominioIdAndEhProprietarioTrueAndAtivoTrue(condominioId)
                .stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public PessoaUnidadeResponse enviarConvite(Long id, Long condominioId) {
        PessoaUnidade pu = getEntity(id, condominioId);

        if (!Boolean.TRUE.equals(pu.getEhMorador())) {
            throw new BadRequestException("Convite so pode ser enviado para moradores.");
        }

        if (pu.getPessoa() == null || pu.getPessoa().getEmail() == null || pu.getPessoa().getEmail().isBlank()) {
            throw new BadRequestException("Morador nao possui e-mail cadastrado.");
        }

        Usuario usuario = resolveUsuarioParaPrimeiroAcesso(pu);
        String senhaTemporaria = generateTemporaryPassword();

        usuario.setNome(pu.getPessoa().getNome());
        usuario.setEmail(pu.getPessoa().getEmail().trim().toLowerCase());
        usuario.setSenha(passwordEncoder.encode(senhaTemporaria));
        usuario.setAtivo(true);
        usuario.setPrimeiroAcesso(true);
        usuario.setCondominioId(pu.getCondominio().getId());
        usuario.setRoles(resolveMoradorRole());
        usuario = usuarioRepository.save(usuario);

        pu.setUsuario(usuario);
        pu.setConviteToken(null);
        pu.setConviteEnviadoEm(LocalDateTime.now());
        pu.setConviteAceitoEm(null);
        pu.setUpdatedAt(LocalDateTime.now());
        repository.save(pu);
        moradorInviteEmailService.sendInvite(pu, senhaTemporaria);

        return mapper.toResponse(pu);
    }

    public void delete(Long id, Long condominioId) {
        PessoaUnidade pu = getEntity(id, condominioId);
        pu.setAtivo(false);
        pu.setUpdatedAt(LocalDateTime.now());
        repository.save(pu);
    }

    public PessoaUnidade getEntity(Long id, Long condominioId) {
        return repository.findByIdAndCondominioId(id, condominioId)
                .orElseThrow(() -> new NotFoundException("Vinculo nao encontrado para este condominio"));
    }

    private Pessoa resolvePessoa(PessoaUnidadeCreateRequest req) {
        if (req.pessoaId() != null) {
            return pessoaRepository.findById(req.pessoaId())
                    .orElseThrow(() -> new NotFoundException("Pessoa nao encontrada"));
        }

        if (req.cpfCnpj() != null && !req.cpfCnpj().isBlank()) {
            return pessoaRepository.findByCpfCnpj(req.cpfCnpj())
                    .orElseGet(() -> criarPessoa(req));
        }

        return criarPessoa(req);
    }

    private Pessoa criarPessoa(PessoaUnidadeCreateRequest req) {
        if (req.nome() == null || req.nome().isBlank()) {
            throw new BadRequestException("Nome e obrigatorio para criar uma pessoa.");
        }

        Condominio condominio = condominioService.getEntity(req.condominioId());

        Pessoa p = new Pessoa();
        p.setCondominio(condominio);
        p.setNome(req.nome());
        p.setCpfCnpj(req.cpfCnpj());
        p.setEmail(req.email());
        p.setTelefone(req.telefone());
        p.setAtivo(true);
        p.setCreatedAt(LocalDateTime.now());
        p.setUpdatedAt(LocalDateTime.now());

        return pessoaRepository.save(p);
    }

    private boolean resolvePrincipalOnCreate(List<PessoaUnidade> vinculosAtivos, Boolean requestedPrincipal) {
        if (!Boolean.TRUE.equals(requestedPrincipal)) {
            return false;
        }

        boolean jaExiste = vinculosAtivos.stream().anyMatch(v -> Boolean.TRUE.equals(v.getPrincipal()));
        return !jaExiste;
    }

    private boolean resolvePrincipalOnUpdate(Long condominioId, Long unidadeId, Long id, Boolean requestedPrincipal) {
        if (!Boolean.TRUE.equals(requestedPrincipal)) {
            return false;
        }

        boolean jaExisteOutro = repository.existsByCondominioIdAndUnidadeIdAndPrincipalTrueAndAtivoTrueAndIdNot(
                condominioId, unidadeId, id
        );
        return !jaExisteOutro;
    }

    private Optional<PessoaUnidade> findVinculoByCpf(List<PessoaUnidade> vinculosAtivos, String cpfCnpj) {
        String normalizedCpf = normalizeCpf(cpfCnpj);
        if (normalizedCpf == null) {
            return Optional.empty();
        }

        return vinculosAtivos.stream()
                .filter(v -> v.getPessoa() != null)
                .filter(v -> Objects.equals(normalizedCpf, normalizeCpf(v.getPessoa().getCpfCnpj())))
                .findFirst();
    }

    private String normalizeCpf(String cpfCnpj) {
        if (cpfCnpj == null || cpfCnpj.isBlank()) {
            return null;
        }

        String digits = cpfCnpj.replaceAll("\\D", "");
        return digits.isBlank() ? null : digits;
    }

    private Usuario resolveUsuarioParaPrimeiroAcesso(PessoaUnidade pu) {
        if (pu.getUsuario() != null) {
            if (!Boolean.TRUE.equals(pu.getUsuario().getPrimeiroAcesso())) {
                throw new BadRequestException("Este morador ja possui uma conta ativa.");
            }
            return pu.getUsuario();
        }

        String email = pu.getPessoa().getEmail().trim().toLowerCase();
        Usuario usuarioExistente = usuarioRepository.findByEmail(email).orElse(null);
        if (usuarioExistente != null) {
            if (!Boolean.TRUE.equals(usuarioExistente.getPrimeiroAcesso())) {
                throw new BadRequestException("Ja existe um usuario cadastrado com este e-mail.");
            }
            return usuarioExistente;
        }

        return Usuario.builder().build();
    }

    private Set<Role> resolveMoradorRole() {
        Role roleMorador = roleRepository.findByNome("MORADOR")
                .orElseThrow(() -> new IllegalStateException("Role MORADOR nao encontrada. Rode o bootstrap de roles."));
        return Set.of(roleMorador);
    }

    private String generateTemporaryPassword() {
        String word = TEMP_PASSWORD_WORDS.get(ThreadLocalRandom.current().nextInt(TEMP_PASSWORD_WORDS.size()));
        int number = ThreadLocalRandom.current().nextInt(1000, 10000);
        return word.substring(0, Math.min(4, word.length())) + number;
    }
}
