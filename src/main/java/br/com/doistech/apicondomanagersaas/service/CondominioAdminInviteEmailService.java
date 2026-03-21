package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.common.exception.BadRequestException;
import br.com.doistech.apicondomanagersaas.domain.condominio.CondominioAdminInvite;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class CondominioAdminInviteEmailService {
    private static final String TEMPLATE_PATH = "templates/emails/condominio-admin-invite.html";

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final String frontendBaseUrl;
    private final String invitePath;

    public CondominioAdminInviteEmailService(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${app.mail.from:no-reply@condomanager.local}") String fromAddress,
            @Value("${app.frontend.base-url:http://localhost:3000}") String frontendBaseUrl,
            @Value("${app.frontend.admin-condominio-convite-path:/convite/admin-condominio}") String invitePath
    ) {
        this.mailSender = mailSenderProvider.getIfAvailable();
        this.fromAddress = fromAddress;
        this.frontendBaseUrl = frontendBaseUrl;
        this.invitePath = invitePath;
    }

    public void sendInvite(CondominioAdminInvite invite) {
        if (mailSender == null) {
            throw new BadRequestException("Servico de e-mail nao configurado. Defina as propriedades SMTP para enviar convites.");
        }

        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());
            helper.setFrom(fromAddress);
            helper.setTo(invite.getEmail());
            helper.setSubject(invite.getCondominio().getNome() + " - Ative seu acesso administrativo");
            helper.setText(buildHtmlBody(invite, buildInviteUrl(invite.getToken())), true);
            mailSender.send(message);
        } catch (MailAuthenticationException ex) {
            throw new BadRequestException("Falha ao autenticar no servidor SMTP. Verifique as credenciais de e-mail.");
        } catch (MailException ex) {
            throw new BadRequestException("Falha ao enviar e-mail de convite do administrador do condominio.");
        } catch (Exception ex) {
            throw new BadRequestException("Falha ao montar o template de e-mail do administrador do condominio.");
        }
    }

    private String buildInviteUrl(String token) {
        String baseUrl = frontendBaseUrl.endsWith("/") ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1) : frontendBaseUrl;
        String path = invitePath.startsWith("/") ? invitePath : "/" + invitePath;
        return baseUrl + path + "?token=" + token;
    }

    private String buildHtmlBody(CondominioAdminInvite invite, String inviteUrl) {
        String template = loadTemplate();
        return template
                .replace("${nomeResponsavel}", escapeHtml(invite.getNome()))
                .replace("${nomeCondominio}", escapeHtml(invite.getCondominio().getNome()))
                .replace("${linkConvite}", escapeHtml(inviteUrl));
    }

    private String loadTemplate() {
        try {
            ClassPathResource resource = new ClassPathResource(TEMPLATE_PATH);
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new BadRequestException("Template de e-mail do administrador do condominio nao encontrado.");
        }
    }

    private String escapeHtml(String value) {
        return HtmlUtils.htmlEscape(value == null ? "" : value, StandardCharsets.UTF_8.name());
    }
}
