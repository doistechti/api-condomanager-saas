package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.domain.condominio.Condominio;
import br.com.doistech.apicondomanagersaas.domain.documentosCondominio.DocumentoCondominio;
import br.com.doistech.apicondomanagersaas.domain.pessoa.Pessoa;
import br.com.doistech.apicondomanagersaas.domain.pessoaUnidade.PessoaUnidade;
import br.com.doistech.apicondomanagersaas.repository.PessoaUnidadeRepository;
import br.com.doistech.apicondomanagersaas.service.email.EmailTemplateService;
import br.com.doistech.apicondomanagersaas.service.email.MailDeliveryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentoEmailServiceTest {

    @Mock
    private MailDeliveryService mailDeliveryService;

    @Mock
    private EmailTemplateService templateService;

    @Mock
    private PessoaUnidadeRepository pessoaUnidadeRepository;

    @InjectMocks
    private DocumentoEmailService service;

    @Test
    void shouldDeduplicateRecipientsWhenDocumentIsActive() {
        DocumentoCondominio documento = DocumentoCondominio.builder()
                .condominio(Condominio.builder().id(1L).nome("Condominio Teste").build())
                .nome("Regimento")
                .arquivoNome("regimento.pdf")
                .arquivoUrl("https://teste/doc.pdf")
                .ativo(true)
                .updatedAt(LocalDateTime.now())
                .build();

        PessoaUnidade pu1 = new PessoaUnidade();
        pu1.setPessoa(Pessoa.builder().email("morador@test.com").build());
        PessoaUnidade pu2 = new PessoaUnidade();
        pu2.setPessoa(Pessoa.builder().email(" morador@test.com ").build());
        PessoaUnidade pu3 = new PessoaUnidade();
        pu3.setPessoa(Pessoa.builder().email("outro@test.com").build());

        when(pessoaUnidadeRepository.findAllByCondominioIdAndAtivoTrue(1L)).thenReturn(List.of(pu1, pu2, pu3));
        when(templateService.render(any(), any(), any())).thenReturn("<p>ok</p>");

        service.sendPublishedNotification(documento);

        ArgumentCaptor<Collection<String>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(mailDeliveryService).sendHtml(captor.capture(), eq("Condominio Teste - Novo documento disponivel"), eq("<p>ok</p>"), eq("notificar documento"));
        assertEquals(2, captor.getValue().size());
        assertTrue(captor.getValue().contains("morador@test.com"));
        assertTrue(captor.getValue().contains("outro@test.com"));
    }

    @Test
    void shouldSkipInactiveDocument() {
        DocumentoCondominio documento = DocumentoCondominio.builder().ativo(false).build();

        service.sendPublishedNotification(documento);

        verify(mailDeliveryService, never()).sendHtml(any(Collection.class), any(), any(), any());
    }
}
