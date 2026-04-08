package br.com.doistech.apicondomanagersaas.service.email;

import br.com.doistech.apicondomanagersaas.domain.notificacao.EmailNotificationLog;
import br.com.doistech.apicondomanagersaas.domain.notificacao.EmailNotificationStatus;
import br.com.doistech.apicondomanagersaas.repository.EmailNotificationLogRepository;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailDeliveryServiceTest {

    @Mock
    private ObjectProvider<JavaMailSender> mailSenderProvider;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private EmailNotificationLogRepository emailNotificationLogRepository;

    @Test
    void shouldMarkNotificationAsSentWhenMailIsDelivered() {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        AtomicReference<EmailNotificationStatus> firstStatus = new AtomicReference<>();
        AtomicReference<EmailNotificationStatus> secondStatus = new AtomicReference<>();

        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(emailNotificationLogRepository.save(any(EmailNotificationLog.class)))
                .thenAnswer(invocation -> {
                    EmailNotificationLog log = invocation.getArgument(0);
                    if (firstStatus.get() == null) {
                        firstStatus.set(log.getStatus());
                    } else {
                        secondStatus.set(log.getStatus());
                    }
                    return log;
                });

        MailDeliveryService service = new MailDeliveryService(mailSenderProvider, emailNotificationLogRepository, "no-reply@test.com");

        service.sendHtml("destino@test.com", "Assunto", "<p>Teste</p>", "teste");

        verify(emailNotificationLogRepository, org.mockito.Mockito.times(2)).save(any(EmailNotificationLog.class));
        verify(mailSender).send(mimeMessage);

        assertEquals(EmailNotificationStatus.PENDENTE, firstStatus.get());
        assertEquals(EmailNotificationStatus.ENVIADO, secondStatus.get());
    }

    @Test
    void shouldMarkNotificationAsFailedWhenMailSenderIsMissing() {
        AtomicReference<EmailNotificationStatus> firstStatus = new AtomicReference<>();
        AtomicReference<EmailNotificationStatus> secondStatus = new AtomicReference<>();
        AtomicReference<String> errorMessage = new AtomicReference<>();

        when(mailSenderProvider.getIfAvailable()).thenReturn(null);
        when(emailNotificationLogRepository.save(any(EmailNotificationLog.class)))
                .thenAnswer(invocation -> {
                    EmailNotificationLog log = invocation.getArgument(0);
                    if (firstStatus.get() == null) {
                        firstStatus.set(log.getStatus());
                    } else {
                        secondStatus.set(log.getStatus());
                        errorMessage.set(log.getErrorMessage());
                    }
                    return log;
                });

        MailDeliveryService service = new MailDeliveryService(mailSenderProvider, emailNotificationLogRepository, "no-reply@test.com");

        service.sendHtml("destino@test.com", "Assunto", "<p>Teste</p>", "teste");

        verify(emailNotificationLogRepository, org.mockito.Mockito.times(2)).save(any(EmailNotificationLog.class));
        verify(mailSender, never()).send(any(MimeMessage.class));

        assertEquals(EmailNotificationStatus.PENDENTE, firstStatus.get());
        assertEquals(EmailNotificationStatus.FALHOU, secondStatus.get());
        assertEquals("Servico de e-mail nao configurado.", errorMessage.get());
    }
}
