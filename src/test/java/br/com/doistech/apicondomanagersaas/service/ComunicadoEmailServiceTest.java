package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.domain.comunicado.Comunicado;
import br.com.doistech.apicondomanagersaas.domain.condominio.Condominio;
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
class ComunicadoEmailServiceTest {

    @Mock
    private MailDeliveryService mailDeliveryService;

    @Mock
    private EmailTemplateService templateService;

    @Mock
    private PessoaUnidadeRepository pessoaUnidadeRepository;

    @InjectMocks
    private ComunicadoEmailService service;

    @Test
    void shouldDeduplicateRecipientsWhenSendingPublishedNotification() {
        Condominio condominio = Condominio.builder().id(1L).nome("Condominio Teste").build();
        Comunicado comunicado = Comunicado.builder()
                .condominio(condominio)
                .titulo("Assembleia")
                .tipo("aviso")
                .conteudo("Conteudo")
                .ativo(true)
                .destaque(true)
                .dataPublicacao(LocalDateTime.now())
                .build();

        Pessoa pessoa1 = Pessoa.builder().email("morador@test.com").build();
        Pessoa pessoa2 = Pessoa.builder().email("morador@test.com").build();
        Pessoa pessoa3 = Pessoa.builder().email(" outro@test.com ").build();

        PessoaUnidade pu1 = new PessoaUnidade();
        pu1.setPessoa(pessoa1);
        PessoaUnidade pu2 = new PessoaUnidade();
        pu2.setPessoa(pessoa2);
        PessoaUnidade pu3 = new PessoaUnidade();
        pu3.setPessoa(pessoa3);

        when(pessoaUnidadeRepository.findAllByCondominioIdAndAtivoTrue(condominio.getId()))
                .thenReturn(List.of(pu1, pu2, pu3));
        when(templateService.render(any(), any(), any())).thenReturn("<p>ok</p>");

        service.sendPublishedNotification(comunicado);

        ArgumentCaptor<Collection<String>> recipientsCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(mailDeliveryService).sendHtml(recipientsCaptor.capture(), eq("Condominio Teste - Novo comunicado"), eq("<p>ok</p>"), eq("notificar comunicado"));

        assertEquals(2, recipientsCaptor.getValue().size());
        assertTrue(recipientsCaptor.getValue().contains("morador@test.com"));
        assertTrue(recipientsCaptor.getValue().contains("outro@test.com"));
    }

    @Test
    void shouldSkipNotificationWhenComunicadoIsInactive() {
        Comunicado comunicado = Comunicado.builder()
                .ativo(false)
                .build();

        service.sendPublishedNotification(comunicado);

        verify(mailDeliveryService, never()).sendHtml(any(List.class), any(), any(), any());
    }
}
