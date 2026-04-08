package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.repository.CondominioRepository;
import br.com.doistech.apicondomanagersaas.domain.usuario.Usuario;
import br.com.doistech.apicondomanagersaas.service.email.EmailTemplateService;
import br.com.doistech.apicondomanagersaas.service.email.MailDeliveryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PasswordResetEmailService {
    private static final String TEMPLATE_PATH = "templates/emails/password-reset.html";

    private final MailDeliveryService mailDeliveryService;
    private final EmailTemplateService templateService;
    private final String frontendBaseUrl;
    private final String resetPasswordPath;
    private final CondominioRepository condominioRepository;

    public PasswordResetEmailService(
            MailDeliveryService mailDeliveryService,
            EmailTemplateService templateService,
            CondominioRepository condominioRepository,
            @Value("${app.frontend.base-url:http://localhost:3000}") String frontendBaseUrl,
            @Value("${app.frontend.reset-password-path:/resetar-senha}") String resetPasswordPath
    ) {
        this.mailDeliveryService = mailDeliveryService;
        this.templateService = templateService;
        this.condominioRepository = condominioRepository;
        this.frontendBaseUrl = frontendBaseUrl;
        this.resetPasswordPath = resetPasswordPath;
    }

    public void sendResetPasswordEmail(Usuario usuario, String token) {
        mailDeliveryService.sendHtml(
                usuario.getEmail(),
                buildSubject(usuario),
                buildHtmlBody(usuario, buildResetUrl(token)),
                "recuperar senha"
        );
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
            return "Condomínio Tech - Recuperação de senha";
        }

        return nomeCondominio + " - Recuperação de senha";
    }

    private String buildHtmlBody(Usuario usuario, String resetUrl) {
        return templateService.render(
                TEMPLATE_PATH,
                Map.of(
                        "nomeUsuario", usuario.getNome(),
                        "linkResetSenha", resetUrl
                ),
                "Template de e-mail de recuperação de senha não encontrado."
        );
    }
}
