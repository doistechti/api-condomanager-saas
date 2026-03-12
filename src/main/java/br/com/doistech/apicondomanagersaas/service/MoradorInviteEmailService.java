package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.domain.pessoaUnidade.PessoaUnidade;
import br.com.doistech.apicondomanagersaas.common.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MoradorInviteEmailService {

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final String frontendBaseUrl;
    private final String moradorInvitePath;

    public MoradorInviteEmailService(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${app.mail.from:no-reply@condomanager.local}") String fromAddress,
            @Value("${app.frontend.base-url:http://localhost:3000}") String frontendBaseUrl,
            @Value("${app.frontend.morador-convite-path:/convite/morador}") String moradorInvitePath
    ) {
        this.mailSender = mailSenderProvider.getIfAvailable();
        this.fromAddress = fromAddress;
        this.frontendBaseUrl = frontendBaseUrl;
        this.moradorInvitePath = moradorInvitePath;
    }

    public void sendInvite(PessoaUnidade pessoaUnidade) {
        if (mailSender == null) {
            throw new BadRequestException("Servico de e-mail nao configurado. Defina as propriedades SMTP para enviar convites.");
        }

        String email = pessoaUnidade.getPessoa().getEmail();
        String subject = "Convite para acessar a plataforma CondoManager";
        String inviteUrl = buildInviteUrl(pessoaUnidade.getConviteToken());

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(email);
        message.setSubject(subject);
        message.setText(buildBody(pessoaUnidade, inviteUrl));

        try {
            mailSender.send(message);
        } catch (MailAuthenticationException ex) {
            throw new IllegalStateException("Falha ao autenticar no servidor SMTP. Verifique as credenciais de e-mail.", ex);
        } catch (MailException ex) {
            throw new IllegalStateException("Falha ao enviar e-mail de convite do morador.", ex);
        }
    }

    private String buildInviteUrl(String token) {
        String baseUrl = frontendBaseUrl.endsWith("/") ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1) : frontendBaseUrl;
        String path = moradorInvitePath.startsWith("/") ? moradorInvitePath : "/" + moradorInvitePath;
        return baseUrl + path + "?token=" + token;
    }

    private String buildBody(PessoaUnidade pessoaUnidade, String inviteUrl) {
        String nome = pessoaUnidade.getPessoa().getNome();
        String unidade = pessoaUnidade.getUnidade().getIdentificacao();
        String condominio = pessoaUnidade.getCondominio().getNome();

        return """
                Ola, %s!

                Voce recebeu um convite para criar sua conta na plataforma CondoManager.

                Condominio: %s
                Unidade: %s

                Para concluir seu cadastro, acesse o link abaixo e defina sua senha:
                %s

                Se voce nao esperava este convite, ignore esta mensagem.
                """.formatted(nome, condominio, unidade, inviteUrl);
    }
}
