package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.domain.comunicado.Comunicado;
import br.com.doistech.apicondomanagersaas.domain.pessoaUnidade.PessoaUnidade;
import br.com.doistech.apicondomanagersaas.repository.PessoaUnidadeRepository;
import br.com.doistech.apicondomanagersaas.service.push.PushMessage;
import br.com.doistech.apicondomanagersaas.service.push.PushNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ComunicadoPushNotificationService {

    private final PushNotificationService pushNotificationService;
    private final PessoaUnidadeRepository pessoaUnidadeRepository;

    public void sendPublishedNotification(Comunicado comunicado) {
        if (!Boolean.TRUE.equals(comunicado.getAtivo())) {
            return;
        }

        Set<Long> usuarioIds = pessoaUnidadeRepository
                .findAllByCondominioIdAndAtivoTrueAndUsuarioIsNotNull(comunicado.getCondominio().getId())
                .stream()
                .map(PessoaUnidade::getUsuario)
                .filter(usuario -> usuario != null)
                .map(usuario -> usuario.getId())
                .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);

        if (usuarioIds.isEmpty()) {
            return;
        }

        pushNotificationService.sendToUsers(
                usuarioIds,
                new PushMessage(
                        comunicado.getDestaque() != null && comunicado.getDestaque()
                                ? "Comunicado em destaque"
                                : "Novo comunicado",
                        buildBody(comunicado),
                        "/morador/documentos-links",
                        "comunicado-" + comunicado.getId()
                )
        );
    }

    private String buildBody(Comunicado comunicado) {
        if (comunicado.getTitulo() == null || comunicado.getTitulo().isBlank()) {
            return "A administração publicou um novo comunicado.";
        }

        return comunicado.getTitulo().trim();
    }
}
