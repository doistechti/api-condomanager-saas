package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.common.exception.BadRequestException;
import br.com.doistech.apicondomanagersaas.common.exception.ForbiddenException;
import br.com.doistech.apicondomanagersaas.common.exception.NotFoundException;
import br.com.doistech.apicondomanagersaas.config.JwtUtil;
import br.com.doistech.apicondomanagersaas.domain.pessoaUnidade.PessoaUnidade;
import br.com.doistech.apicondomanagersaas.domain.role.Role;
import br.com.doistech.apicondomanagersaas.domain.usuario.Usuario;
import br.com.doistech.apicondomanagersaas.dto.auth.LoginRequest;
import br.com.doistech.apicondomanagersaas.dto.auth.LoginResponse;
import br.com.doistech.apicondomanagersaas.dto.auth.MeResponse;
import br.com.doistech.apicondomanagersaas.dto.auth.MoradorInviteAcceptRequest;
import br.com.doistech.apicondomanagersaas.dto.auth.MoradorInviteResponse;
import br.com.doistech.apicondomanagersaas.repository.PessoaUnidadeRepository;
import br.com.doistech.apicondomanagersaas.repository.RoleRepository;
import br.com.doistech.apicondomanagersaas.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PessoaUnidadeRepository pessoaUnidadeRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final int moradorInviteExpirationHours;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PessoaUnidadeRepository pessoaUnidadeRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            @Value("${app.convite.morador.expiration-hours:72}") int moradorInviteExpirationHours
    ) {
        this.usuarioRepository = usuarioRepository;
        this.pessoaUnidadeRepository = pessoaUnidadeRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.moradorInviteExpirationHours = moradorInviteExpirationHours;
    }

    public LoginResponse login(LoginRequest req) {
        Usuario usuario = usuarioRepository.findByEmail(req.email())
                .orElseThrow(() -> new RuntimeException("Credenciais invalidas"));

        if (Boolean.FALSE.equals(usuario.getAtivo())) {
            throw new ForbiddenException("Conta aguardando liberacao do Trial pelo gestor do SaaS.");
        }

        if (!passwordEncoder.matches(req.senha(), usuario.getSenha())) {
            throw new RuntimeException("Credenciais invalidas");
        }

        List<String> roles = usuario.getRoles().stream().map(Role::getNome).toList();
        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getId(), usuario.getCondominioId(), roles);

        return new LoginResponse(
                token,
                new LoginResponse.UsuarioMeResponse(
                        usuario.getId(),
                        usuario.getNome(),
                        usuario.getEmail(),
                        usuario.getCondominioId(),
                        roles
                )
        );
    }

    public MeResponse me(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));

        List<String> roles = usuario.getRoles().stream().map(Role::getNome).toList();
        return new MeResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getCondominioId(), roles);
    }

    @Transactional(readOnly = true)
    public MoradorInviteResponse getMoradorInvite(String token) {
        PessoaUnidade vinculo = getValidInvite(token);
        return new MoradorInviteResponse(
                vinculo.getConviteToken(),
                vinculo.getPessoa().getNome(),
                vinculo.getPessoa().getEmail(),
                vinculo.getCondominio().getNome(),
                vinculo.getUnidade().getIdentificacao(),
                vinculo.getConviteEnviadoEm()
        );
    }

    @Transactional
    public LoginResponse acceptMoradorInvite(MoradorInviteAcceptRequest request) {
        PessoaUnidade vinculo = getValidInvite(request.token());

        if (vinculo.getUsuario() != null) {
            throw new BadRequestException("Este convite ja foi utilizado.");
        }

        String email = normalizeEmail(vinculo.getPessoa().getEmail());
        if (email == null) {
            throw new BadRequestException("Morador nao possui e-mail cadastrado.");
        }

        Usuario usuarioExistente = usuarioRepository.findByEmail(email).orElse(null);
        if (usuarioExistente != null) {
            throw new BadRequestException("Ja existe um usuario cadastrado com este e-mail.");
        }

        Role roleMorador = roleRepository.findByNome("MORADOR")
                .orElseThrow(() -> new IllegalStateException("Role MORADOR nao encontrada. Rode o bootstrap de roles."));

        Usuario usuario = Usuario.builder()
                .nome(vinculo.getPessoa().getNome())
                .email(email)
                .senha(passwordEncoder.encode(request.senha()))
                .ativo(true)
                .condominioId(vinculo.getCondominio().getId())
                .roles(Set.of(roleMorador))
                .build();
        usuario = usuarioRepository.save(usuario);

        vinculo.setUsuario(usuario);
        vinculo.setConviteAceitoEm(LocalDateTime.now());
        vinculo.setConviteToken(null);
        vinculo.setUpdatedAt(LocalDateTime.now());
        pessoaUnidadeRepository.save(vinculo);

        List<String> roles = usuario.getRoles().stream().map(Role::getNome).toList();
        String jwt = jwtUtil.generateToken(usuario.getEmail(), usuario.getId(), usuario.getCondominioId(), roles);

        return new LoginResponse(
                jwt,
                new LoginResponse.UsuarioMeResponse(
                        usuario.getId(),
                        usuario.getNome(),
                        usuario.getEmail(),
                        usuario.getCondominioId(),
                        roles
                )
        );
    }

    private PessoaUnidade getValidInvite(String token) {
        if (token == null || token.isBlank()) {
            throw new BadRequestException("Token de convite e obrigatorio.");
        }

        PessoaUnidade vinculo = pessoaUnidadeRepository.findByConviteToken(token)
                .orElseThrow(() -> new NotFoundException("Convite nao encontrado."));

        if (!Boolean.TRUE.equals(vinculo.getEhMorador()) || !Boolean.TRUE.equals(vinculo.getAtivo())) {
            throw new BadRequestException("Convite invalido para cadastro de morador.");
        }

        if (vinculo.getConviteEnviadoEm() == null) {
            throw new BadRequestException("Convite invalido.");
        }

        if (vinculo.getConviteAceitoEm() != null) {
            throw new BadRequestException("Este convite ja foi utilizado.");
        }

        if (vinculo.getConviteEnviadoEm().plusHours(Math.max(moradorInviteExpirationHours, 1)).isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Este convite expirou. Solicite um novo envio.");
        }

        return vinculo;
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String normalized = email.trim().toLowerCase();
        return normalized.isBlank() ? null : normalized;
    }
}
