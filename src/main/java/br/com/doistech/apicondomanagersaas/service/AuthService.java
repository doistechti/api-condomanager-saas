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
import br.com.doistech.apicondomanagersaas.dto.auth.FirstAccessPasswordRequest;
import br.com.doistech.apicondomanagersaas.dto.auth.MoradorInviteAcceptRequest;
import br.com.doistech.apicondomanagersaas.dto.auth.MoradorInviteResponse;
import br.com.doistech.apicondomanagersaas.dto.auth.ForgotPasswordRequest;
import br.com.doistech.apicondomanagersaas.dto.auth.ResetPasswordRequest;
import br.com.doistech.apicondomanagersaas.repository.PessoaUnidadeRepository;
import br.com.doistech.apicondomanagersaas.repository.RoleRepository;
import br.com.doistech.apicondomanagersaas.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PessoaUnidadeRepository pessoaUnidadeRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final int moradorInviteExpirationHours;
    private final int resetPasswordExpirationMinutes;
    private final PasswordResetEmailService passwordResetEmailService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PessoaUnidadeRepository pessoaUnidadeRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            PasswordResetEmailService passwordResetEmailService,
            @Value("${app.convite.morador.expiration-hours:72}") int moradorInviteExpirationHours,
            @Value("${app.auth.reset-password.expiration-minutes:30}") int resetPasswordExpirationMinutes
    ) {
        this.usuarioRepository = usuarioRepository;
        this.pessoaUnidadeRepository = pessoaUnidadeRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.moradorInviteExpirationHours = moradorInviteExpirationHours;
        this.resetPasswordExpirationMinutes = resetPasswordExpirationMinutes;
        this.passwordResetEmailService = passwordResetEmailService;
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

        if (!Boolean.TRUE.equals(usuario.getPrimeiroAcesso()) && usuario.getPrimeiroAcessoConcluidoEm() == null) {
            usuario.setPrimeiroAcessoConcluidoEm(LocalDateTime.now());
            usuarioRepository.save(usuario);
        }

        List<String> roles = usuario.getRoles().stream().map(Role::getNome).toList();
        String displayName = resolveDisplayName(usuario, roles);
        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getId(), usuario.getCondominioId(), roles);

        return buildLoginResponse(usuario, displayName, roles, token);
    }

    public MeResponse me(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));

        List<String> roles = usuario.getRoles().stream().map(Role::getNome).toList();
        String displayName = resolveDisplayName(usuario, roles);
        return new MeResponse(
                usuario.getId(),
                displayName,
                usuario.getEmail(),
                usuario.getCondominioId(),
                roles,
                Boolean.TRUE.equals(usuario.getPrimeiroAcesso())
        );
    }

    @Transactional
    public void requestPasswordReset(ForgotPasswordRequest request) {
        String email = normalizeEmail(request.email());
        if (email == null) {
            return;
        }

        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        if (usuario == null || Boolean.FALSE.equals(usuario.getAtivo())) {
            return;
        }

        String rawToken = UUID.randomUUID().toString();
        usuario.setResetSenhaTokenHash(hashToken(rawToken));
        usuario.setResetSenhaExpiraEm(LocalDateTime.now().plusMinutes(Math.max(resetPasswordExpirationMinutes, 1)));
        usuarioRepository.save(usuario);

        passwordResetEmailService.sendResetPasswordEmail(usuario, rawToken);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (request.token() == null || request.token().isBlank()) {
            throw new BadRequestException("Token de recuperacao e obrigatorio.");
        }

        String tokenHash = hashToken(request.token());
        Usuario usuario = usuarioRepository.findByResetSenhaTokenHash(tokenHash)
                .orElseThrow(() -> new BadRequestException("Token de recuperacao invalido ou expirado."));

        if (usuario.getResetSenhaExpiraEm() == null || usuario.getResetSenhaExpiraEm().isBefore(LocalDateTime.now())) {
            clearResetPasswordToken(usuario);
            usuarioRepository.save(usuario);
            throw new BadRequestException("Token de recuperacao invalido ou expirado.");
        }

        usuario.setSenha(passwordEncoder.encode(request.senha()));
        clearResetPasswordToken(usuario);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void completeFirstAccess(String email, FirstAccessPasswordRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));

        if (!Boolean.TRUE.equals(usuario.getPrimeiroAcesso())) {
            throw new BadRequestException("Este usuario ja concluiu o primeiro acesso.");
        }

        usuario.setSenha(passwordEncoder.encode(request.senha()));
        usuario.setPrimeiroAcesso(false);
        usuario.setPrimeiroAcessoConcluidoEm(LocalDateTime.now());
        usuarioRepository.save(usuario);

        pessoaUnidadeRepository.findAllByUsuarioIdAndEhMoradorTrueAndAtivoTrue(usuario.getId()).stream()
                .findFirst()
                .ifPresent(vinculo -> {
                    vinculo.setConviteAceitoEm(LocalDateTime.now());
                    vinculo.setUpdatedAt(LocalDateTime.now());
                    pessoaUnidadeRepository.save(vinculo);
                });
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
                .primeiroAcesso(false)
                .primeiroAcessoConcluidoEm(LocalDateTime.now())
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
        String displayName = resolveDisplayName(usuario, roles);
        String jwt = jwtUtil.generateToken(usuario.getEmail(), usuario.getId(), usuario.getCondominioId(), roles);

        return buildLoginResponse(usuario, displayName, roles, jwt);
    }

    private LoginResponse buildLoginResponse(Usuario usuario, String displayName, List<String> roles, String token) {
        return new LoginResponse(
                token,
                new LoginResponse.UsuarioMeResponse(
                        usuario.getId(),
                        displayName,
                        usuario.getEmail(),
                        usuario.getCondominioId(),
                        roles,
                        Boolean.TRUE.equals(usuario.getPrimeiroAcesso())
                )
        );
    }

    private String resolveDisplayName(Usuario usuario, List<String> roles) {
        if (roles.contains("MORADOR")) {
            return pessoaUnidadeRepository.findAllByUsuarioIdAndEhMoradorTrueAndAtivoTrue(usuario.getId()).stream()
                    .sorted((a, b) -> Boolean.compare(Boolean.TRUE.equals(b.getPrincipal()), Boolean.TRUE.equals(a.getPrincipal())))
                    .map(PessoaUnidade::getPessoa)
                    .filter(pessoa -> pessoa != null && pessoa.getNome() != null && !pessoa.getNome().isBlank())
                    .map(pessoa -> pessoa.getNome().trim())
                    .findFirst()
                    .orElse(usuario.getNome());
        }

        return usuario.getNome();
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

    private void clearResetPasswordToken(Usuario usuario) {
        usuario.setResetSenhaTokenHash(null);
        usuario.setResetSenhaExpiraEm(null);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.trim().getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Algoritmo de hash nao suportado.", ex);
        }
    }
}
