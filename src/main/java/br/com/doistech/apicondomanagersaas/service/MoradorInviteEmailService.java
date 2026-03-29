package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.domain.pessoaUnidade.PessoaUnidade;
import br.com.doistech.apicondomanagersaas.common.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
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
public class MoradorInviteEmailService {
    private static final String TEMPLATE_PATH = "templates/emails/morador-invite.html";

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final String frontendBaseUrl;
    private final String mobileAppUrl;

    public MoradorInviteEmailService(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${app.mail.from:no-reply@condomanager.local}") String fromAddress,
            @Value("${app.frontend.base-url:http://localhost:3000}") String frontendBaseUrl,
            @Value("${app.frontend.mobile-url:https://condoapp.doistech.com.br}") String mobileAppUrl
    ) {
        this.mailSender = mailSenderProvider.getIfAvailable();
        this.fromAddress = fromAddress;
        this.frontendBaseUrl = frontendBaseUrl;
        this.mobileAppUrl = mobileAppUrl;
    }

    public void sendInvite(PessoaUnidade pessoaUnidade, String senhaTemporaria) {
        if (mailSender == null) {
            throw new BadRequestException("Servico de e-mail nao configurado. Defina as propriedades SMTP para enviar convites.");
        }

        String email = pessoaUnidade.getPessoa().getEmail();
        String subject = buildSubject(pessoaUnidade);

        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());
            helper.setFrom(fromAddress);
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(buildHtmlBody(pessoaUnidade, senhaTemporaria), true);
            mailSender.send(message);
        } catch (MailAuthenticationException ex) {
            throw new BadRequestException("Falha ao autenticar no servidor SMTP. Verifique as credenciais de e-mail.");
        } catch (MailException ex) {
            throw new BadRequestException("Falha ao enviar e-mail de convite do morador.");
        } catch (Exception ex) {
            throw new BadRequestException("Falha ao montar o template de e-mail do convite do morador.");
        }
    }

    private String buildSubject(PessoaUnidade pessoaUnidade) {
        String nomeCondominio = pessoaUnidade.getCondominio() != null ? pessoaUnidade.getCondominio().getNome() : "";
        return nomeCondominio + " - Seu primeiro acesso ao Condomínio Tech.";
    }

    private String buildHtmlBody(PessoaUnidade pessoaUnidade, String senhaTemporaria) {
        String template = loadTemplate();
        return template
                .replace("${nomeMorador}", escapeHtml(pessoaUnidade.getPessoa().getNome()))
                .replace("${nomeCondominio}", escapeHtml(pessoaUnidade.getCondominio().getNome()))
                .replace("${emailMorador}", escapeHtml(pessoaUnidade.getPessoa().getEmail()))
                .replace("${numeroUnidade}", escapeHtml(pessoaUnidade.getUnidade().getIdentificacao()))
                .replace("${senhaTemporaria}", escapeHtml(senhaTemporaria))
                .replace("${portalWebUrl}", escapeHtml(normalizeUrl(frontendBaseUrl)))
                .replace("${appUrl}", escapeHtml(normalizeUrl(mobileAppUrl)));
    }

    private String loadTemplate() {
        try {
            ClassPathResource resource = new ClassPathResource(TEMPLATE_PATH);
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new BadRequestException("Template de e-mail do convite do morador nao encontrado.");
        }
    }

    private String escapeHtml(String value) {
        return HtmlUtils.htmlEscape(value == null ? "" : value, StandardCharsets.UTF_8.name());
    }

    private String normalizeUrl(String value) {
        if (value == null) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
