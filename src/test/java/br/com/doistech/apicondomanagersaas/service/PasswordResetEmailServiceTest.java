package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.domain.condominio.Condominio;
import br.com.doistech.apicondomanagersaas.domain.usuario.Usuario;
import br.com.doistech.apicondomanagersaas.dto.auth.PasswordResetProject;
import br.com.doistech.apicondomanagersaas.repository.CondominioRepository;
import br.com.doistech.apicondomanagersaas.service.email.EmailTemplateService;
import br.com.doistech.apicondomanagersaas.service.email.MailDeliveryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetEmailServiceTest {

    @Mock
    private MailDeliveryService mailDeliveryService;

    @Mock
    private EmailTemplateService templateService;

    @Mock
    private CondominioRepository condominioRepository;

    private PasswordResetEmailService service;

    @BeforeEach
    void setUp() {
        service = new PasswordResetEmailService(
                mailDeliveryService,
                templateService,
                condominioRepository,
                "https://condominiotech.doistech.com.br/",
                "https://condoapp.doistech.com.br",
                "resetar-senha"
        );
    }

    @Test
    void shouldRenderTemplateAndSendResetEmailWithFrontendLink() {
        Usuario usuario = Usuario.builder()
                .id(10L)
                .nome("Maria Silva")
                .email("maria@teste.com")
                .condominioId(3L)
                .build();

        when(condominioRepository.findById(3L))
                .thenReturn(Optional.of(Condominio.builder().id(3L).nome("Residencial Solar").build()));
        when(templateService.render(any(), any(), any())).thenReturn("<p>html</p>");

        service.sendResetPasswordEmail(usuario, "raw-reset-token", PasswordResetProject.WEB);

        ArgumentCaptor<Map<String, String>> variablesCaptor = ArgumentCaptor.forClass(Map.class);
        verify(templateService).render(
                eq("templates/emails/password-reset.html"),
                variablesCaptor.capture(),
                eq("Template de e-mail de recuperação de senha não encontrado.")
        );

        assertEquals("Maria Silva", variablesCaptor.getValue().get("nomeUsuario"));
        assertEquals(
                "https://condominiotech.doistech.com.br/resetar-senha?token=raw-reset-token",
                variablesCaptor.getValue().get("linkResetSenha")
        );

        verify(mailDeliveryService).sendHtml(
                eq("maria@teste.com"),
                eq("Residencial Solar - Recuperação de senha"),
                eq("<p>html</p>"),
                eq("recuperar senha")
        );
    }

    @Test
    void shouldFallbackToPlatformSubjectWhenCondominioIsUnavailable() {
        Usuario usuario = Usuario.builder()
                .nome("Carlos")
                .email("carlos@teste.com")
                .condominioId(null)
                .build();

        when(templateService.render(any(), any(), any())).thenReturn("<p>html</p>");

        service.sendResetPasswordEmail(usuario, "token", PasswordResetProject.WEB);

        verify(mailDeliveryService).sendHtml(
                eq("carlos@teste.com"),
                eq("Condomínio Tech - Recuperação de senha"),
                eq("<p>html</p>"),
                eq("recuperar senha")
        );
    }

    @Test
    void shouldUseMobileBaseUrlWhenProjectIsMobile() {
        Usuario usuario = Usuario.builder()
                .nome("Ana")
                .email("ana@teste.com")
                .condominioId(null)
                .build();

        when(templateService.render(any(), any(), any())).thenReturn("<p>html</p>");

        service.sendResetPasswordEmail(usuario, "mobile-token", PasswordResetProject.MOBILE);

        ArgumentCaptor<Map<String, String>> variablesCaptor = ArgumentCaptor.forClass(Map.class);
        verify(templateService).render(
                eq("templates/emails/password-reset.html"),
                variablesCaptor.capture(),
                eq("Template de e-mail de recuperação de senha não encontrado.")
        );

        assertEquals(
                "https://condoapp.doistech.com.br/resetar-senha?token=mobile-token",
                variablesCaptor.getValue().get("linkResetSenha")
        );
    }
}
