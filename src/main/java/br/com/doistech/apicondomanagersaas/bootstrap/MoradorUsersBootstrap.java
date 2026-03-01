package br.com.doistech.apicondomanagersaas.bootstrap;

import br.com.doistech.apicondomanagersaas.domain.condominio.Condominio;
import br.com.doistech.apicondomanagersaas.domain.pessoa.Pessoa;
import br.com.doistech.apicondomanagersaas.domain.pessoaUnidade.MoradorTipo;
import br.com.doistech.apicondomanagersaas.domain.pessoaUnidade.PessoaUnidade;
import br.com.doistech.apicondomanagersaas.domain.role.Role;
import br.com.doistech.apicondomanagersaas.domain.unidade.Unidade;
import br.com.doistech.apicondomanagersaas.domain.usuario.Usuario;
import br.com.doistech.apicondomanagersaas.domain.vinculo.TipoMoradia;
import br.com.doistech.apicondomanagersaas.domain.vinculo.VinculoUnidade;
import br.com.doistech.apicondomanagersaas.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static br.com.doistech.apicondomanagersaas.bootstrap.CondominioBootstrap.CONDOMINIO_TESTE_NOME;

@Order(4)
@Component
public class MoradorUsersBootstrap implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(MoradorUsersBootstrap.class);

    // ✅ credenciais padrão para DEV
    private static final String EMAIL_MORADOR = "morador@teste.com";
    private static final String SENHA_MORADOR = "teste234";

    // ✅ dados do morador teste (idempotente)
    private static final String PESSOA_NOME = "Morador Teste";
    private static final String PESSOA_CPF = "11122233344";
    private static final String PESSOA_TELEFONE = "21999990000";

    // ✅ unidade teste
    private static final String UNIDADE_IDENT = "101";
    private static final String UNIDADE_DESC = "Unidade bootstrap Morador";

    private final CondominioRepository condominioRepository;
    private final UnidadeRepository unidadeRepository;
    private final PessoaRepository pessoaRepository;
    private final PessoaUnidadeRepository pessoaUnidadeRepository;
    private final VinculoUnidadeRepository vinculoUnidadeRepository;
    private final UsuarioRepository usuarioRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public MoradorUsersBootstrap(
            CondominioRepository condominioRepository,
            UnidadeRepository unidadeRepository,
            PessoaRepository pessoaRepository,
            PessoaUnidadeRepository pessoaUnidadeRepository,
            VinculoUnidadeRepository vinculoUnidadeRepository,
            UsuarioRepository usuarioRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.condominioRepository = condominioRepository;
        this.unidadeRepository = unidadeRepository;
        this.pessoaRepository = pessoaRepository;
        this.pessoaUnidadeRepository = pessoaUnidadeRepository;
        this.vinculoUnidadeRepository = vinculoUnidadeRepository;
        this.usuarioRepository = usuarioRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {

        log.info("Iniciando bootstrap de MORADOR (usuário teste + vínculo)...");

        Condominio condominio = condominioRepository.findByNome(CONDOMINIO_TESTE_NOME)
                .orElseThrow(() -> new IllegalStateException("Condomínio teste não encontrado. Bootstrap de condomínio falhou?"));

        // 1) garantir unidade no condomínio
        Unidade unidade = unidadeRepository.findAllByCondominioId(condominio.getId()).stream()
                .filter(u -> UNIDADE_IDENT.equalsIgnoreCase(u.getIdentificacao()))
                .findFirst()
                .orElseGet(() -> {
                    Unidade nova = Unidade.builder()
                            .condominio(condominio)
                            .identificacao(UNIDADE_IDENT)
                            .descricao(UNIDADE_DESC)
                            .ativo(true)
                            .build();

                    Unidade salva = unidadeRepository.save(nova);
                    log.info("Unidade teste criada (id={}, ident={})", salva.getId(), salva.getIdentificacao());
                    return salva;
                });

        // 2) garantir pessoa
        Pessoa pessoa = pessoaRepository.findByCpfCnpj(PESSOA_CPF)
                .orElseGet(() -> {
                    Pessoa nova = Pessoa.builder()
                            .nome(PESSOA_NOME)
                            .cpfCnpj(PESSOA_CPF)
                            .email(EMAIL_MORADOR)
                            .telefone(PESSOA_TELEFONE)
                            .ativo(true)
                            .condominio(condominio)
                            .build();

                    Pessoa salva = pessoaRepository.save(nova);
                    log.info("Pessoa do morador criada (id={}, cpf={})", salva.getId(), PESSOA_CPF);
                    return salva;
                });

        // 3) garantir usuário MORADOR
        Usuario usuario = usuarioRepository.findByEmail(EMAIL_MORADOR)
                .orElseGet(() -> {
                    Role roleMorador = roleRepository.findByNome("MORADOR")
                            .orElseThrow(() -> new IllegalStateException("Role MORADOR não encontrada (RolesBootstrap falhou?)"));

                    Usuario novo = new Usuario();
                    novo.setNome(PESSOA_NOME);
                    novo.setEmail(EMAIL_MORADOR);
                    novo.setSenha(passwordEncoder.encode(SENHA_MORADOR));
                    novo.setAtivo(true);
                    novo.setCondominioId(condominio.getId());
                    novo.setRoles(Set.of(roleMorador));

                    Usuario salvo = usuarioRepository.save(novo);
                    log.info("Usuário MORADOR criado (id={}, email={})", salvo.getId(), EMAIL_MORADOR);
                    return salvo;
                });

        // 4) garantir vínculo PessoaUnidade com usuario_id
        boolean jaTemVinculo = pessoaUnidadeRepository.findAllByCondominioIdAndUnidadeIdAndAtivoTrue(condominio.getId(), unidade.getId())
                .stream()
                .anyMatch(v -> v.getPessoa().getId().equals(pessoa.getId()) && Boolean.TRUE.equals(v.getEhMorador()));

        if (jaTemVinculo) {
            log.info("Vínculo morador já existe — verificando se usuario_id está ligado...");

            pessoaUnidadeRepository.findAllByCondominioIdAndUnidadeIdAndAtivoTrue(condominio.getId(), unidade.getId())
                    .stream()
                    .filter(v -> v.getPessoa().getId().equals(pessoa.getId()) && Boolean.TRUE.equals(v.getEhMorador()))
                    .findFirst()
                    .ifPresent(v -> {
                        if (v.getUsuario() == null) {
                            v.setUsuario(usuario);
                            pessoaUnidadeRepository.save(v);
                            log.info("Vínculo atualizado: usuario_id associado (vinculoId={})", v.getId());
                        } else {
                            log.info("Vínculo já possui usuario_id (vinculoId={})", v.getId());
                        }
                    });

            // ✅ Garantir (e corrigir) o vínculo operacional em vinculos_unidade para RESERVAS
            ensureVinculoOperacional(condominio, unidade, pessoa);

            log.info("Bootstrap MORADOR finalizado");
            return;
        }

        PessoaUnidade vinculo = new PessoaUnidade();
        vinculo.setCondominio(condominio);
        vinculo.setUnidade(unidade);
        vinculo.setPessoa(pessoa);

        vinculo.setEhMorador(true);
        vinculo.setMoradorTipo(MoradorTipo.PROPRIETARIO);
        vinculo.setEhProprietario(true);

        vinculo.setPrincipal(true);
        vinculo.setAtivo(true);
        vinculo.setDataInicio(LocalDate.now());

        vinculo.setUsuario(usuario);

        PessoaUnidade salvo = pessoaUnidadeRepository.save(vinculo);
        log.info("Vínculo PessoaUnidade criado (id={}, usuarioId={}, pessoaId={}, unidadeId={})",
                salvo.getId(), usuario.getId(), pessoa.getId(), unidade.getId());

        // ✅ Garantir (e corrigir) o vínculo operacional em vinculos_unidade para RESERVAS
        ensureVinculoOperacional(condominio, unidade, pessoa);

        log.info("Bootstrap MORADOR finalizado");
    }

    private void ensureVinculoOperacional(Condominio condominio, Unidade unidade, Pessoa pessoa) {
        VinculoUnidade v = vinculoUnidadeRepository
                .findByCondominioIdAndPessoaIdAndUnidadeId(condominio.getId(), pessoa.getId(), unidade.getId())
                .orElseGet(() -> {
                    VinculoUnidade novo = VinculoUnidade.builder()
                            .condominio(condominio)
                            .unidade(unidade)
                            .pessoa(pessoa)
                            .isMorador(true)
                            .isProprietario(true)
                            .tipoMoradia(TipoMoradia.PROPRIETARIO)
                            .dataInicio(LocalDate.now())
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    VinculoUnidade salvo = vinculoUnidadeRepository.save(novo);
                    log.info("VinculoUnidade criado (id={})", salvo.getId());
                    return salvo;
                });

        // ✅ Se já existia mas estava incorreto (causando 'Apenas moradores podem reservar'), corrigimos.
        boolean mudou = false;
        if (!v.isMorador()) {
            v.setMorador(true);
            mudou = true;
        }
        if (!v.isProprietario()) {
            v.setProprietario(true);
            mudou = true;
        }
        if (v.getTipoMoradia() == null) {
            v.setTipoMoradia(TipoMoradia.PROPRIETARIO);
            mudou = true;
        }
        if (v.getDataInicio() == null) {
            v.setDataInicio(LocalDate.now());
            mudou = true;
        }
        v.setUpdatedAt(LocalDateTime.now());
        if (v.getCreatedAt() == null) {
            v.setCreatedAt(LocalDateTime.now());
        }

        if (mudou) {
            vinculoUnidadeRepository.save(v);
            log.info("VinculoUnidade corrigido para permitir reservas (id={})", v.getId());
        }
    }
}