package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.domain.comunicado.Comunicado;
import br.com.doistech.apicondomanagersaas.domain.pessoaUnidade.PessoaUnidade;
import br.com.doistech.apicondomanagersaas.repository.PessoaUnidadeRepository;
import br.com.doistech.apicondomanagersaas.service.email.EmailTemplateService;
import br.com.doistech.apicondomanagersaas.service.email.MailDeliveryService;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Service
public class ComunicadoEmailService {

    private static final String TEMPLATE_PATH = "templates/emails/comunicado-publicado.html";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final MailDeliveryService mailDeliveryService;
    private final EmailTemplateService templateService;
    private final PessoaUnidadeRepository pessoaUnidadeRepository;

    public ComunicadoEmailService(
            MailDeliveryService mailDeliveryService,
            EmailTemplateService templateService,
            PessoaUnidadeRepository pessoaUnidadeRepository
    ) {
        this.mailDeliveryService = mailDeliveryService;
        this.templateService = templateService;
        this.pessoaUnidadeRepository = pessoaUnidadeRepository;
    }

    public void sendPublishedNotification(Comunicado comunicado) {
        if (!Boolean.TRUE.equals(comunicado.getAtivo())) {
            return;
        }

        Set<String> recipients = pessoaUnidadeRepository.findAllByCondominioIdAndAtivoTrue(comunicado.getCondominio().getId())
                .stream()
                .map(PessoaUnidade::getPessoa)
                .filter(pessoa -> pessoa != null)
                .map(pessoa -> normalizeEmail(pessoa.getEmail()))
                .filter(email -> email != null)
                .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);

        if (recipients.isEmpty()) {
            return;
        }

        String subject = comunicado.getCondominio().getNome() + " - Novo comunicado";
        String html = templateService.render(
                TEMPLATE_PATH,
                Map.of(
                        "nomeCondominio", comunicado.getCondominio().getNome(),
                        "tituloComunicado", comunicado.getTitulo(),
                        "tipoComunicado", blankIfNull(comunicado.getTipo()),
                        "conteudoComunicado", blankIfNull(comunicado.getConteudo()),
                        "dataPublicacao", comunicado.getDataPublicacao() == null
                                ? ""
                                : comunicado.getDataPublicacao().format(DATE_TIME_FORMATTER),
                        "dataExpiracao", comunicado.getDataExpiracao() == null
                                ? ""
                                : comunicado.getDataExpiracao().format(DATE_TIME_FORMATTER),
                        "destaqueComunicado", Boolean.TRUE.equals(comunicado.getDestaque()) ? "Sim" : "Não"
                ),
                "Template de e-mail de comunicado não encontrado."
        );

        mailDeliveryService.sendHtml(recipients, subject, html, "notificar comunicado");
    }

    private String blankIfNull(String value) {
        return value == null ? "" : value;
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String normalized = email.trim().toLowerCase();
        return normalized.isBlank() ? null : normalized;
    }
}
