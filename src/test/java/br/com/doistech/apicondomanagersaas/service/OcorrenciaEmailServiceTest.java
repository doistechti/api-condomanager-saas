package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.domain.condominio.Condominio;
import br.com.doistech.apicondomanagersaas.domain.ocorrencia.Ocorrencia;
import br.com.doistech.apicondomanagersaas.domain.ocorrencia.OcorrenciaCategoria;
import br.com.doistech.apicondomanagersaas.domain.ocorrencia.OcorrenciaMensagem;
import br.com.doistech.apicondomanagersaas.domain.ocorrencia.OcorrenciaStatus;
import br.com.doistech.apicondomanagersaas.domain.pessoa.Pessoa;
import br.com.doistech.apicondomanagersaas.domain.pessoaUnidade.PessoaUnidade;
import br.com.doistech.apicondomanagersaas.domain.unidade.Unidade;
import br.com.doistech.apicondomanagersaas.repository.UsuarioRepository;
import br.com.doistech.apicondomanagersaas.service.email.EmailTemplateService;
import br.com.doistech.apicondomanagersaas.service.email.MailDeliveryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OcorrenciaEmailServiceTest {

    @Mock
    private MailDeliveryService mailDeliveryService;

    @Mock
    private EmailTemplateService templateService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private OcorrenciaEmailService service;

    @Test
    void shouldNotifyMoradorAndAdminsWhenAdminReplies() {
        Ocorrencia ocorrencia = buildOcorrencia("morador@test.com");
        OcorrenciaMensagem mensagem = OcorrenciaMensagem.builder().mensagem("Resposta").build();
        when(templateService.render(any(), any(), any())).thenReturn("<p>ok</p>");
        when(usuarioRepository.findActiveAdminEmailsByCondominioId(1L))
                .thenReturn(List.of("admin@test.com", " outro@test.com "));

        service.sendAdminReplyNotifications(ocorrencia, mensagem);

        verify(mailDeliveryService).sendHtml("morador@test.com", "Nova resposta na ocorrencia OCR-000001", "<p>ok</p>", "notificar a ocorrencia do morador");

        ArgumentCaptor<Collection<String>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(mailDeliveryService).sendHtml(captor.capture(), eq("Resposta enviada na ocorrencia OCR-000001"), eq("<p>ok</p>"), eq("notificar a ocorrencia para administradores"));
        assertEquals(2, captor.getValue().size());
        assertTrue(captor.getValue().contains("admin@test.com"));
        assertTrue(captor.getValue().contains("outro@test.com"));
    }

    private Ocorrencia buildOcorrencia(String emailMorador) {
        Condominio condominio = Condominio.builder().id(1L).nome("Condominio Teste").build();
        Pessoa pessoa = Pessoa.builder().nome("Morador").email(emailMorador).build();
        Unidade unidade = Unidade.builder().identificacao("A-101").build();
        PessoaUnidade vinculo = new PessoaUnidade();
        vinculo.setPessoa(pessoa);
        vinculo.setUnidade(unidade);

        return Ocorrencia.builder()
                .codigo("OCR-000001")
                .condominio(condominio)
                .moradorVinculo(vinculo)
                .unidade(unidade)
                .categoria(OcorrenciaCategoria.outros)
                .titulo("Titulo")
                .descricao("Descricao")
                .status(OcorrenciaStatus.aberta)
                .build();
    }
}
