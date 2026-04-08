package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.domain.documentosCondominio.DocumentoCondominio;
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
public class DocumentoEmailService {

    private static final String TEMPLATE_PATH = "templates/emails/documento-publicado.html";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final MailDeliveryService mailDeliveryService;
    private final EmailTemplateService templateService;
    private final PessoaUnidadeRepository pessoaUnidadeRepository;

    public DocumentoEmailService(
            MailDeliveryService mailDeliveryService,
            EmailTemplateService templateService,
            PessoaUnidadeRepository pessoaUnidadeRepository
    ) {
        this.mailDeliveryService = mailDeliveryService;
        this.templateService = templateService;
        this.pessoaUnidadeRepository = pessoaUnidadeRepository;
    }

    public void sendPublishedNotification(DocumentoCondominio documento) {
        if (!Boolean.TRUE.equals(documento.getAtivo())) {
            return;
        }

        Set<String> recipients = pessoaUnidadeRepository.findAllByCondominioIdAndAtivoTrue(documento.getCondominio().getId())
                .stream()
                .map(PessoaUnidade::getPessoa)
                .filter(pessoa -> pessoa != null)
                .map(pessoa -> normalizeEmail(pessoa.getEmail()))
                .filter(email -> email != null)
                .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);

        if (recipients.isEmpty()) {
            return;
        }

        String subject = documento.getCondominio().getNome() + " - Novo documento disponível";
        String html = templateService.render(
                TEMPLATE_PATH,
                Map.of(
                        "nomeCondominio", documento.getCondominio().getNome(),
                        "nomeDocumento", documento.getNome(),
                        "arquivoNome", documento.getArquivoNome(),
                        "categoriaDocumento", blankIfNull(documento.getCategoria()),
                        "descricaoDocumento", blankIfNull(documento.getDescricao()),
                        "linkDocumento", documento.getArquivoUrl(),
                        "dataAtualizacao", documento.getUpdatedAt() == null
                                ? ""
                                : documento.getUpdatedAt().format(DATE_TIME_FORMATTER)
                ),
                "Template de e-mail de documento não encontrado."
        );

        mailDeliveryService.sendHtml(recipients, subject, html, "notificar documento");
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
