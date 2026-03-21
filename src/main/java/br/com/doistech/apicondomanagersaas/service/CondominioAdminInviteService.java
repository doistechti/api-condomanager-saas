package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.common.exception.BadRequestException;
import br.com.doistech.apicondomanagersaas.common.exception.NotFoundException;
import br.com.doistech.apicondomanagersaas.config.JwtUtil;
import br.com.doistech.apicondomanagersaas.domain.condominio.Condominio;
import br.com.doistech.apicondomanagersaas.domain.condominio.CondominioAdminInvite;
import br.com.doistech.apicondomanagersaas.domain.role.Role;
import br.com.doistech.apicondomanagersaas.domain.usuario.Usuario;
import br.com.doistech.apicondomanagersaas.dto.auth.CondominioAdminInviteAcceptRequest;
import br.com.doistech.apicondomanagersaas.dto.auth.CondominioAdminInviteResponse;
import br.com.doistech.apicondomanagersaas.dto.auth.LoginResponse;
import br.com.doistech.apicondomanagersaas.repository.CondominioAdminInviteRepository;
import br.com.doistech.apicondomanagersaas.repository.RoleRepository;
import br.com.doistech.apicondomanagersaas.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class CondominioAdminInviteService {

    private final CondominioAdminInviteRepository inviteRepository;
    private final UsuarioRepository usuarioRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CondominioAdminInviteEmailService emailService;
    private final int expirationHours;

    public CondominioAdminInviteService(
            CondominioAdminInviteRepository inviteRepository,
            UsuarioRepository usuarioRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            CondominioAdminInviteEmailService emailService,
            @Value("${app.convite.admin-condominio.expiration-hours:72}") int expirationHours
    ) {
        this.inviteRepository = inviteRepository;
        this.usuarioRepository = usuarioRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
        this.expirationHours = expirationHours;
    }

    @Transactional
    public void createAndSendInviteIfPossible(Condominio condominio) {
        String email = normalizeEmail(condominio.getEmail());
        if (email == null) {
            return;
        }
        if (usuarioRepository.existsByEmail(email)) {
            throw new BadRequestException("Ja existe um usuario cadastrado com este e-mail.");
        }
        createOrReusePendingInvite(condominio, condominio.getResponsavel(), email);
    }

    @Transactional
    public CondominioAdminInviteResponse resendInvite(Long condominioId, Condominio condominio) {
        String email = normalizeEmail(condominio.getEmail());
        if (email == null) {
            throw new BadRequestException("Condominio nao possui e-mail cadastrado para envio do convite.");
        }

        if (usuarioRepository.existsByEmail(email)) {
            throw new BadRequestException("Ja existe um usuario cadastrado com este e-mail.");
        }

        CondominioAdminInvite invite = inviteRepository
                .findTopByCondominioIdAndAtivoTrueAndAceitoEmIsNullOrderByCreatedAtDesc(condominioId)
                .filter(existing -> existing.getExpiraEm() != null && existing.getExpiraEm().isAfter(LocalDateTime.now()))
                .map(existing -> reusePendingInvite(existing, condominio.getResponsavel(), email))
                .orElseGet(() -> createNewInvite(condominio, condominio.getResponsavel(), email));

        emailService.sendInvite(invite);
        return toResponse(invite);
    }

    @Transactional(readOnly = true)
    public CondominioAdminInviteResponse getInvite(String token) {
        CondominioAdminInvite invite = getValidInvite(token);
        return new CondominioAdminInviteResponse(
                invite.getToken(),
                invite.getNome(),
                invite.getEmail(),
                invite.getCondominio().getNome(),
                invite.getEnviadoEm()
        );
    }

    @Transactional
    public LoginResponse acceptInvite(CondominioAdminInviteAcceptRequest request) {
        CondominioAdminInvite invite = getValidInvite(request.token());
        String email = normalizeEmail(invite.getEmail());
        if (email == null) {
            throw new BadRequestException("Convite invalido.");
        }

        if (invite.getUsuario() != null || invite.getAceitoEm() != null) {
            throw new BadRequestException("Este convite ja foi utilizado.");
        }

        if (usuarioRepository.existsByEmail(email)) {
            throw new BadRequestException("Ja existe um usuario cadastrado com este e-mail.");
        }

        Role role = roleRepository.findByNome("ADMIN_CONDOMINIO")
                .orElseThrow(() -> new IllegalStateException("Role ADMIN_CONDOMINIO nao encontrada."));

        Usuario usuario = Usuario.builder()
                .nome(invite.getNome())
                .email(email)
                .senha(passwordEncoder.encode(request.senha()))
                .ativo(true)
                .condominioId(invite.getCondominio().getId())
                .roles(Set.of(role))
                .build();
        usuario = usuarioRepository.save(usuario);

        invite.setUsuario(usuario);
        invite.setAceitoEm(LocalDateTime.now());
        invite.setAtivo(false);
        invite.setToken(UUID.randomUUID().toString());
        invite.setUpdatedAt(LocalDateTime.now());
        inviteRepository.save(invite);

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

    private CondominioAdminInvite createOrReusePendingInvite(Condominio condominio, String nome, String email) {
        CondominioAdminInvite invite = inviteRepository
                .findTopByCondominioIdAndAtivoTrueAndAceitoEmIsNullOrderByCreatedAtDesc(condominio.getId())
                .filter(existing -> existing.getExpiraEm() != null && existing.getExpiraEm().isAfter(LocalDateTime.now()))
                .map(existing -> reusePendingInvite(existing, nome, email))
                .orElseGet(() -> createNewInvite(condominio, nome, email));

        emailService.sendInvite(invite);
        return invite;
    }

    private CondominioAdminInvite createNewInvite(Condominio condominio, String nome, String email) {
        CondominioAdminInvite invite = CondominioAdminInvite.builder()
                .condominio(condominio)
                .nome(resolveName(nome, condominio.getNome()))
                .email(email)
                .token(UUID.randomUUID().toString())
                .enviadoEm(LocalDateTime.now())
                .expiraEm(LocalDateTime.now().plusHours(Math.max(expirationHours, 1)))
                .ativo(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return inviteRepository.save(invite);
    }

    private CondominioAdminInvite reusePendingInvite(CondominioAdminInvite invite, String nome, String email) {
        invite.setNome(resolveName(nome, invite.getCondominio().getNome()));
        invite.setEmail(email);
        invite.setEnviadoEm(LocalDateTime.now());
        invite.setUpdatedAt(LocalDateTime.now());
        return inviteRepository.save(invite);
    }

    private CondominioAdminInviteResponse toResponse(CondominioAdminInvite invite) {
        return new CondominioAdminInviteResponse(
                invite.getToken(),
                invite.getNome(),
                invite.getEmail(),
                invite.getCondominio().getNome(),
                invite.getEnviadoEm()
        );
    }

    private CondominioAdminInvite getValidInvite(String token) {
        if (token == null || token.isBlank()) {
            throw new BadRequestException("Token de convite e obrigatorio.");
        }

        CondominioAdminInvite invite = inviteRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Convite nao encontrado."));

        if (!Boolean.TRUE.equals(invite.getAtivo())) {
            throw new BadRequestException("Este convite nao esta mais ativo.");
        }

        if (invite.getAceitoEm() != null) {
            throw new BadRequestException("Este convite ja foi utilizado.");
        }

        if (invite.getExpiraEm() == null || invite.getExpiraEm().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Este convite expirou. Solicite um novo envio.");
        }

        return invite;
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String normalized = email.trim().toLowerCase();
        return normalized.isBlank() ? null : normalized;
    }

    private String resolveName(String nome, String fallback) {
        if (nome == null || nome.isBlank()) {
            return fallback == null || fallback.isBlank() ? "Administrador" : fallback.trim();
        }
        return nome.trim();
    }
}
