package br.com.doistech.apicondomanagersaas.controller;

import br.com.doistech.apicondomanagersaas.domain.chat.ConversaStatus;
import br.com.doistech.apicondomanagersaas.domain.chat.ConversaTipo;
import br.com.doistech.apicondomanagersaas.dto.chat.*;
import br.com.doistech.apicondomanagersaas.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/condominios/{condominioId}")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService service;

    @GetMapping("/conversas")
    public List<ConversaResponse> listConversas(
            @PathVariable Long condominioId,
            @RequestParam(required = false) ConversaStatus status,
            @RequestParam(required = false) ConversaTipo tipo
    ) {
        return service.listConversas(condominioId, tipo, status);
    }

    @PostMapping("/conversas")
    @ResponseStatus(HttpStatus.CREATED)
    public ConversaResponse createConversa(@PathVariable Long condominioId, @Valid @RequestBody ConversaCreateRequest req) {
        return service.createConversa(condominioId, req);
    }

    @PatchMapping("/conversas/{id}/fechar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void fechar(@PathVariable Long condominioId, @PathVariable Long id) {
        service.fecharConversa(condominioId, id);
    }

    @GetMapping("/conversas/unread-count")
    public UnreadCountResponse unreadCount(
            @PathVariable Long condominioId,
            @RequestParam(required = false) ConversaTipo tipo
    ) {
        return service.unreadCount(condominioId, tipo);
    }

    @GetMapping("/conversas/{id}/mensagens")
    public List<MensagemResponse> listMensagens(@PathVariable Long condominioId, @PathVariable Long id) {
        return service.listMensagens(condominioId, id);
    }

    @PostMapping("/conversas/{id}/mensagens")
    @ResponseStatus(HttpStatus.CREATED)
    public MensagemResponse send(@PathVariable Long condominioId, @PathVariable Long id, @Valid @RequestBody MensagemCreateRequest req) {
        return service.sendMensagem(condominioId, id, req);
    }

    @PatchMapping("/conversas/{id}/mensagens/lidas")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAsRead(@PathVariable Long condominioId, @PathVariable Long id) {
        service.markAsRead(condominioId, id);
    }
}