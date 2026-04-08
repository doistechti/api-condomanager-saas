package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.domain.assinatura.Assinatura;
import br.com.doistech.apicondomanagersaas.domain.assinatura.AssinaturaStatus;
import br.com.doistech.apicondomanagersaas.domain.condominio.Condominio;
import br.com.doistech.apicondomanagersaas.domain.plano.Plano;
import br.com.doistech.apicondomanagersaas.repository.UsuarioRepository;
import br.com.doistech.apicondomanagersaas.service.email.EmailTemplateService;
import br.com.doistech.apicondomanagersaas.service.email.MailDeliveryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssinaturaEmailServiceTest {

    @Mock
    private MailDeliveryService mailDeliveryService;

    @Mock
    private EmailTemplateService templateService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Test
    void shouldNotifyDeduplicatedAdminsWhenSubscriptionIsDueSoon() {
        AssinaturaEmailService service = new AssinaturaEmailService(mailDeliveryService, templateService, usuarioRepository, 7);
        Assinatura assinatura = Assinatura.builder()
                .condominio(Condominio.builder().id(1L).nome("Condominio Teste").build())
                .plano(Plano.builder().nome("Essencial").build())
                .status(AssinaturaStatus.ATIVO)
                .dataInicio(LocalDate.of(2026, 4, 1))
                .dataVencimento(LocalDate.now().plusDays(7))
                .build();

        when(usuarioRepository.findActiveAdminEmailsByCondominioId(1L))
                .thenReturn(List.of("admin@test.com", " ADMIN@test.com ", "outro@test.com"));
        when(templateService.render(any(), any(), any())).thenReturn("<p>ok</p>");

        service.sendDueSoonNotification(assinatura);

        ArgumentCaptor<Collection<String>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(mailDeliveryService).sendHtml(captor.capture(), eq("Condominio Teste - Assinatura proxima do vencimento"), eq("<p>ok</p>"), eq("notificar assinatura"));
        assertEquals(2, captor.getValue().size());
        assertTrue(captor.getValue().contains("admin@test.com"));
        assertTrue(captor.getValue().contains("outro@test.com"));
    }
}
