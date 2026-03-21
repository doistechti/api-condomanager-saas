package br.com.doistech.apicondomanagersaas.controller;

import br.com.doistech.apicondomanagersaas.dto.auth.CondominioAdminInviteAcceptRequest;
import br.com.doistech.apicondomanagersaas.dto.auth.CondominioAdminInviteResponse;
import br.com.doistech.apicondomanagersaas.dto.auth.LoginResponse;
import br.com.doistech.apicondomanagersaas.service.CondominioAdminInviteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/public/convites/admin-condominio")
@RequiredArgsConstructor
public class PublicCondominioAdminInviteController {

    private final CondominioAdminInviteService inviteService;

    @GetMapping("/{token}")
    public CondominioAdminInviteResponse getInvite(@PathVariable String token) {
        return inviteService.getInvite(token);
    }

    @PostMapping("/aceitar")
    @ResponseStatus(HttpStatus.CREATED)
    public LoginResponse acceptInvite(@Valid @RequestBody CondominioAdminInviteAcceptRequest request) {
        return inviteService.acceptInvite(request);
    }
}
