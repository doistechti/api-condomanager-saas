package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.common.exception.BadRequestException;
import br.com.doistech.apicondomanagersaas.config.JwtUtil;
import br.com.doistech.apicondomanagersaas.domain.usuario.Usuario;
import br.com.doistech.apicondomanagersaas.dto.auth.ForgotPasswordRequest;
import br.com.doistech.apicondomanagersaas.dto.auth.PasswordResetProject;
import br.com.doistech.apicondomanagersaas.dto.auth.ResetPasswordRequest;
import br.com.doistech.apicondomanagersaas.repository.PessoaUnidadeRepository;
import br.com.doistech.apicondomanagersaas.repository.RoleRepository;
import br.com.doistech.apicondomanagersaas.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServicePasswordResetTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PessoaUnidadeRepository pessoaUnidadeRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordResetEmailService passwordResetEmailService;

    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(
                usuarioRepository,
                pessoaUnidadeRepository,
                roleRepository,
                passwordEncoder,
                jwtUtil,
                passwordResetEmailService,
                72,
                30
        );
    }

    @Test
    void shouldStoreHashedTokenAndSendResetEmailForActiveUser() {
        Usuario usuario = Usuario.builder()
                .id(1L)
                .nome("Joao")
                .email("joao@teste.com")
                .ativo(true)
                .build();

        when(usuarioRepository.findByEmail("joao@teste.com")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.requestPasswordReset(new ForgotPasswordRequest("  JOAO@teste.com ", PasswordResetProject.MOBILE));

        assertNotNull(usuario.getResetSenhaTokenHash());
        assertNotNull(usuario.getResetSenhaExpiraEm());
        assertTrue(usuario.getResetSenhaExpiraEm().isAfter(LocalDateTime.now().plusMinutes(29)));

        ArgumentCaptor<String> rawTokenCaptor = ArgumentCaptor.forClass(String.class);
        verify(passwordResetEmailService).sendResetPasswordEmail(any(Usuario.class), rawTokenCaptor.capture(), eq(PasswordResetProject.MOBILE));
        assertEquals(sha256(rawTokenCaptor.getValue()), usuario.getResetSenhaTokenHash());
    }

    @Test
    void shouldIgnoreForgotPasswordWhenUserDoesNotExist() {
        when(usuarioRepository.findByEmail("inexistente@teste.com")).thenReturn(Optional.empty());

        service.requestPasswordReset(new ForgotPasswordRequest("inexistente@teste.com", PasswordResetProject.WEB));

        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(passwordResetEmailService, never()).sendResetPasswordEmail(any(Usuario.class), any(String.class), any(PasswordResetProject.class));
    }

    @Test
    void shouldResetPasswordAndClearTokenWhenTokenIsValid() {
        String rawToken = "token-valido";
        Usuario usuario = Usuario.builder()
                .id(2L)
                .nome("Maria")
                .email("maria@teste.com")
                .senha("hash-antigo")
                .resetSenhaTokenHash(sha256(rawToken))
                .resetSenhaExpiraEm(LocalDateTime.now().plusMinutes(10))
                .build();

        when(usuarioRepository.findByResetSenhaTokenHash(sha256(rawToken))).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("NovaSenha123")).thenReturn("hash-novo");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.resetPassword(new ResetPasswordRequest(rawToken, "NovaSenha123"));

        assertEquals("hash-novo", usuario.getSenha());
        assertNull(usuario.getResetSenhaTokenHash());
        assertNull(usuario.getResetSenhaExpiraEm());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void shouldRejectExpiredTokenAndClearStoredResetData() {
        String rawToken = "token-expirado";
        Usuario usuario = Usuario.builder()
                .id(3L)
                .nome("Paula")
                .email("paula@teste.com")
                .resetSenhaTokenHash(sha256(rawToken))
                .resetSenhaExpiraEm(LocalDateTime.now().minusMinutes(1))
                .build();

        when(usuarioRepository.findByResetSenhaTokenHash(sha256(rawToken))).thenReturn(Optional.of(usuario));

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> service.resetPassword(new ResetPasswordRequest(rawToken, "NovaSenha123"))
        );

        assertEquals("Token de recuperacao invalido ou expirado.", ex.getMessage());
        assertNull(usuario.getResetSenhaTokenHash());
        assertNull(usuario.getResetSenhaExpiraEm());
        verify(usuarioRepository).save(usuario);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.trim().getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
