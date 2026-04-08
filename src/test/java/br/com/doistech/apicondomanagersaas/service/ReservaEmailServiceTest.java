package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.domain.condominio.Condominio;
import br.com.doistech.apicondomanagersaas.domain.espaco.Espaco;
import br.com.doistech.apicondomanagersaas.domain.pessoa.Pessoa;
import br.com.doistech.apicondomanagersaas.domain.reserva.Reserva;
import br.com.doistech.apicondomanagersaas.domain.reserva.ReservaStatus;
import br.com.doistech.apicondomanagersaas.domain.unidade.Unidade;
import br.com.doistech.apicondomanagersaas.domain.vinculo.VinculoUnidade;
import br.com.doistech.apicondomanagersaas.repository.UsuarioRepository;
import br.com.doistech.apicondomanagersaas.service.email.EmailTemplateService;
import br.com.doistech.apicondomanagersaas.service.email.MailDeliveryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaEmailServiceTest {

    @Mock
    private MailDeliveryService mailDeliveryService;

    @Mock
    private EmailTemplateService templateService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ReservaEmailService service;

    @Test
    void shouldNotifyMoradorAndDeduplicatedAdminsOnReservationCreate() {
        Reserva reserva = buildReserva("morador@test.com");
        when(templateService.render(any(), any(), any())).thenReturn("<p>ok</p>");
        when(usuarioRepository.findActiveAdminEmailsByCondominioId(1L))
                .thenReturn(List.of("admin@test.com", "ADMIN@test.com", " outro@test.com "));

        service.sendCreatedNotifications(reserva);

        verify(mailDeliveryService).sendHtml("morador@test.com", "Condominio Teste - Reserva pendente", "<p>ok</p>", "notificar a reserva do morador");

        ArgumentCaptor<Collection<String>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(mailDeliveryService).sendHtml(captor.capture(), eq("Nova reserva pendente"), eq("<p>ok</p>"), eq("notificar a reserva para administradores"));
        assertEquals(2, captor.getValue().size());
        assertTrue(captor.getValue().contains("admin@test.com"));
        assertTrue(captor.getValue().contains("outro@test.com"));
    }

    private Reserva buildReserva(String emailMorador) {
        Condominio condominio = Condominio.builder().id(1L).nome("Condominio Teste").build();
        Pessoa pessoa = Pessoa.builder().nome("Morador").email(emailMorador).build();
        Unidade unidade = Unidade.builder().identificacao("A-101").build();
        VinculoUnidade vinculo = VinculoUnidade.builder().pessoa(pessoa).unidade(unidade).build();
        Espaco espaco = Espaco.builder().nome("Salao").build();

        return Reserva.builder()
                .condominio(condominio)
                .vinculo(vinculo)
                .espaco(espaco)
                .status(ReservaStatus.PENDENTE)
                .dataReserva(LocalDate.of(2026, 4, 10))
                .build();
    }
}
