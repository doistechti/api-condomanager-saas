package br.com.doistech.apicondomanagersaas.service.email;

import br.com.doistech.apicondomanagersaas.domain.notificacao.EmailNotificationLog;
import br.com.doistech.apicondomanagersaas.domain.notificacao.EmailNotificationStatus;
import br.com.doistech.apicondomanagersaas.repository.EmailNotificationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Collection;

@Service
public class MailDeliveryService {

    private static final Logger log = LoggerFactory.getLogger(MailDeliveryService.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final EmailNotificationLogRepository emailNotificationLogRepository;

    public MailDeliveryService(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            EmailNotificationLogRepository emailNotificationLogRepository,
            @Value("${app.mail.from:no-reply@condomanager.local}") String fromAddress
    ) {
        this.mailSender = mailSenderProvider.getIfAvailable();
        this.emailNotificationLogRepository = emailNotificationLogRepository;
        this.fromAddress = fromAddress;
    }

    @Async("mailTaskExecutor")
    @Transactional
    public void sendHtml(String to, String subject, String htmlBody, String contextLabel) {
        sendHtmlInternal(new String[]{to}, subject, htmlBody, contextLabel);
    }

    @Async("mailTaskExecutor")
    @Transactional
    public void sendHtml(Collection<String> recipients, String subject, String htmlBody, String contextLabel) {
        sendHtmlInternal(recipients.toArray(String[]::new), subject, htmlBody, contextLabel);
    }

    private void sendHtmlInternal(String[] recipients, String subject, String htmlBody, String contextLabel) {
        String joinedRecipients = String.join(", ", recipients);
        EmailNotificationLog logEntry = emailNotificationLogRepository.save(
                EmailNotificationLog.builder()
                        .contextLabel(contextLabel)
                        .recipients(joinedRecipients)
                        .subject(subject)
                        .status(EmailNotificationStatus.PENDENTE)
                        .build()
        );

        if (mailSender == null) {
            markFailed(logEntry, "Servico de e-mail nao configurado.");
            log.warn("Servico de e-mail nao configurado para {}. Destinatarios: {}", contextLabel, joinedRecipients);
            return;
        }

        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());
            helper.setFrom(fromAddress);
            helper.setTo(recipients);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            markSent(logEntry);
            log.debug("E-mail enviado para {} no contexto {}", joinedRecipients, contextLabel);
        } catch (MailAuthenticationException ex) {
            markFailed(logEntry, "Falha ao autenticar no servidor SMTP.");
            log.error("Falha ao autenticar no SMTP para {}. Destinatarios: {}", contextLabel, joinedRecipients, ex);
        } catch (MailException ex) {
            markFailed(logEntry, "Falha ao enviar e-mail.");
            log.error("Falha ao enviar e-mail para {}. Destinatarios: {}", contextLabel, joinedRecipients, ex);
        } catch (Exception ex) {
            markFailed(logEntry, "Falha ao montar o e-mail.");
            log.error("Falha ao montar o e-mail para {}. Destinatarios: {}", contextLabel, joinedRecipients, ex);
        }
    }

    private void markSent(EmailNotificationLog logEntry) {
        logEntry.setStatus(EmailNotificationStatus.ENVIADO);
        logEntry.setErrorMessage(null);
        emailNotificationLogRepository.save(logEntry);
    }

    private void markFailed(EmailNotificationLog logEntry, String errorMessage) {
        logEntry.setStatus(EmailNotificationStatus.FALHOU);
        logEntry.setErrorMessage(truncate(errorMessage, 1000));
        emailNotificationLogRepository.save(logEntry);
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
