package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.common.exception.BadRequestException;
import br.com.doistech.apicondomanagersaas.repository.CondominioRepository;
import br.com.doistech.apicondomanagersaas.domain.usuario.Usuario;
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
public class PasswordResetEmailService {
    private static final String TEMPLATE_PATH = "templates/emails/password-reset.html";

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final String frontendBaseUrl;
    private final String resetPasswordPath;
    private final CondominioRepository condominioRepository;

    public PasswordResetEmailService(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            CondominioRepository condominioRepository,
            @Value("${app.mail.from:no-reply@condomanager.local}") String fromAddress,
            @Value("${app.frontend.base-url:http://localhost:3000}") String frontendBaseUrl,
            @Value("${app.frontend.reset-password-path:/resetar-senha}") String resetPasswordPath
    ) {
        this.mailSender = mailSenderProvider.getIfAvailable();
        this.condominioRepository = condominioRepository;
        this.fromAddress = fromAddress;
        this.frontendBaseUrl = frontendBaseUrl;
        this.resetPasswordPath = resetPasswordPath;
    }

    public void sendResetPasswordEmail(Usuario usuario, String token) {
        if (mailSender == null) {
            throw new BadRequestException("Servico de e-mail nao configurado. Defina as propriedades SMTP para recuperar senha.");
        }

        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());
            helper.setFrom(fromAddress);
            helper.setTo(usuario.getEmail());
            helper.setSubject(buildSubject(usuario));
            helper.setText(buildHtmlBody(usuario, buildResetUrl(token)), true);
            mailSender.send(message);
        } catch (MailAuthenticationException ex) {
            throw new BadRequestException("Falha ao autenticar no servidor SMTP. Verifique as credenciais de e-mail.");
        } catch (MailException ex) {
            throw new BadRequestException("Falha ao enviar e-mail de recuperacao de senha.");
        } catch (Exception ex) {
            throw new BadRequestException("Falha ao montar o template de e-mail de recuperacao de senha.");
        }
    }

    private String buildResetUrl(String token) {
        String baseUrl = frontendBaseUrl.endsWith("/") ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1) : frontendBaseUrl;
        String path = resetPasswordPath.startsWith("/") ? resetPasswordPath : "/" + resetPasswordPath;
        return baseUrl + path + "?token=" + token;
    }

    private String buildSubject(Usuario usuario) {
        String nomeCondominio = null;
        if (usuario.getCondominioId() != null) {
            nomeCondominio = condominioRepository.findById(usuario.getCondominioId())
                    .map(condominio -> condominio.getNome())
                    .orElse(null);
        }

        if (nomeCondominio == null || nomeCondominio.isBlank()) {
            return "Condominio Tech - Recuperacao de senha";
        }

        return nomeCondominio + " - Recuperacao de senha";
    }

    private String buildHtmlBody(Usuario usuario, String resetUrl) {
        String template = loadTemplate();
        return template
                .replace("${nomeUsuario}", escapeHtml(usuario.getNome()))
                .replace("${linkResetSenha}", escapeHtml(resetUrl));
    }

    private String loadTemplate() {
        try {
            ClassPathResource resource = new ClassPathResource(TEMPLATE_PATH);
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new BadRequestException("Template de e-mail de recuperacao de senha nao encontrado.");
        }
    }

    private String escapeHtml(String value) {
        return HtmlUtils.htmlEscape(value == null ? "" : value, StandardCharsets.UTF_8.name());
    }
}
