package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.domain.documentosCondominio.DocumentoCondominio;
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
public class DocumentoPushNotificationService {

    private final PushNotificationService pushNotificationService;
    private final PessoaUnidadeRepository pessoaUnidadeRepository;

    public void sendPublishedNotification(DocumentoCondominio documento) {
        if (!Boolean.TRUE.equals(documento.getAtivo())) {
            return;
        }

        Set<Long> usuarioIds = pessoaUnidadeRepository
                .findAllByCondominioIdAndAtivoTrueAndUsuarioIsNotNull(documento.getCondominio().getId())
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
                        "Novo documento disponível",
                        buildBody(documento),
                        "/morador/documentos-links",
                        "documento-" + documento.getId()
                )
        );
    }

    private String buildBody(DocumentoCondominio documento) {
        if (documento.getNome() == null || documento.getNome().isBlank()) {
            return "A administração publicou um novo documento.";
        }

        return documento.getNome().trim();
    }
}
