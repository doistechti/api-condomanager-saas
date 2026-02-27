package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.common.exception.ForbiddenException;
import br.com.doistech.apicondomanagersaas.config.JwtUtil;
import br.com.doistech.apicondomanagersaas.domain.usuario.Usuario;
import br.com.doistech.apicondomanagersaas.dto.auth.LoginRequest;
import br.com.doistech.apicondomanagersaas.dto.auth.LoginResponse;
import br.com.doistech.apicondomanagersaas.dto.auth.MeResponse;
import br.com.doistech.apicondomanagersaas.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest req) {
        Usuario u = usuarioRepository.findByEmail(req.email())
                .orElseThrow(() -> new RuntimeException("Credenciais inválidas"));

        if (Boolean.FALSE.equals(u.getAtivo())) {
            throw new ForbiddenException("Conta aguardando liberação do Trial pelo gestor do SaaS.");
        }

        if (!passwordEncoder.matches(req.senha(), u.getSenha())) {
            throw new RuntimeException("Credenciais inválidas");
        }

        List<String> roles = u.getRoles().stream().map(r -> r.getNome()).toList();
        String token = jwtUtil.generateToken(u.getEmail(), u.getId(), u.getCondominioId(), roles);

        return new LoginResponse(
                token,
                new LoginResponse.UsuarioMeResponse(u.getId(), u.getNome(), u.getEmail(), u.getCondominioId(), roles)
        );
    }

    public MeResponse me(String email) {
        Usuario u = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        List<String> roles = u.getRoles().stream().map(r -> r.getNome()).toList();
        return new MeResponse(u.getId(), u.getNome(), u.getEmail(), u.getCondominioId(), roles);
    }
}

