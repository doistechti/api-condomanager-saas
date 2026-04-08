package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.common.exception.BadRequestException;
import br.com.doistech.apicondomanagersaas.common.exception.ForbiddenException;
import br.com.doistech.apicondomanagersaas.domain.pessoaUnidade.MoradorTipo;
import br.com.doistech.apicondomanagersaas.domain.pessoaUnidade.PessoaUnidade;
import br.com.doistech.apicondomanagersaas.dto.morador.MoradorManagedCreateRequest;
import br.com.doistech.apicondomanagersaas.dto.pessoaunidade.PessoaUnidadeCreateRequest;
import br.com.doistech.apicondomanagersaas.dto.pessoaunidade.PessoaUnidadeResponse;
import br.com.doistech.apicondomanagersaas.dto.pessoaunidade.PessoaUnidadeUpdateRequest;
import br.com.doistech.apicondomanagersaas.mapper.PessoaUnidadeMapper;
import br.com.doistech.apicondomanagersaas.repository.PessoaUnidadeRepository;
import br.com.doistech.apicondomanagersaas.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MoradorManagementService {

    private final UsuarioRepository usuarioRepository;
    private final PessoaUnidadeRepository pessoaUnidadeRepository;
    private final PessoaUnidadeService pessoaUnidadeService;
    private final PessoaUnidadeMapper pessoaUnidadeMapper;
    private final MoradorScopeService moradorScopeService;

    public List<PessoaUnidadeResponse> listManagedMoradores(String email) {
        var scope = moradorScopeService.getScope(email);
        var unidadeIds = getManageableUnitIds(email);
        if (unidadeIds.isEmpty()) {
          return List.of();
        }

        return pessoaUnidadeRepository.findAllByCondominioIdAndUnidadeIdInAndAtivoTrue(scope.condominioId(), unidadeIds)
                .stream()
                .filter(vinculo -> Boolean.TRUE.equals(vinculo.getEhMorador()))
                .sorted(Comparator
                        .comparing((PessoaUnidade vinculo) -> !Boolean.TRUE.equals(vinculo.getPrincipal()))
                        .thenComparing(vinculo -> vinculo.getPessoa() != null ? vinculo.getPessoa().getNome() : "")
                        .thenComparing(PessoaUnidade::getId))
                .map(pessoaUnidadeMapper::toResponse)
                .toList();
    }

    public PessoaUnidadeResponse createMorador(String email, MoradorManagedCreateRequest req) {
        validateCreateRequest(req);
        var scope = moradorScopeService.getScope(email);
        assertCanManageUnit(email, req.unidadeId());

        MoradorTipo tipo = req.moradorTipo() != null ? req.moradorTipo() : MoradorTipo.DEPENDENTE;
        if (tipo != MoradorTipo.DEPENDENTE) {
            throw new BadRequestException("Neste portal, o morador principal pode cadastrar apenas dependentes.");
        }

        return pessoaUnidadeService.create(new PessoaUnidadeCreateRequest(
                scope.condominioId(),
                req.unidadeId(),
                null,
                req.nome(),
                req.cpfCnpj(),
                req.email(),
                req.telefone(),
                req.fotoUrl(),
                req.fotoNome(),
                false,
                true,
                tipo,
                false,
                req.dataInicio(),
                req.dataFim()
        ));
    }

    public PessoaUnidadeResponse updateMorador(String email, Long id, MoradorManagedCreateRequest req) {
        validateCreateRequest(req);
        var scope = moradorScopeService.getScope(email);
        PessoaUnidade vinculo = pessoaUnidadeService.getEntity(id, scope.condominioId());
        assertCanManageUnit(email, vinculo.getUnidade().getId());

        if (!Boolean.TRUE.equals(vinculo.getEhMorador()) || vinculo.getMoradorTipo() != MoradorTipo.DEPENDENTE) {
            throw new BadRequestException("Este portal permite editar apenas dependentes.");
        }
        if (Boolean.TRUE.equals(vinculo.getPrincipal())) {
            throw new BadRequestException("Nao e permitido editar o morador principal por este fluxo.");
        }

        String currentEmail = normalizeEmail(vinculo.getPessoa() != null ? vinculo.getPessoa().getEmail() : null);
        String requestedEmail = normalizeEmail(req.email());
        boolean emailChanged = !Objects.equals(currentEmail, requestedEmail);

        if (emailChanged && hasActiveAccount(vinculo)) {
            throw new BadRequestException("Nao e permitido alterar o e-mail de um dependente com conta ativa.");
        }

        PessoaUnidadeResponse updated = pessoaUnidadeService.update(id, scope.condominioId(), new PessoaUnidadeUpdateRequest(
                req.nome(),
                req.cpfCnpj(),
                req.email(),
                req.telefone(),
                req.fotoUrl(),
                req.fotoNome(),
                false,
                true,
                MoradorTipo.DEPENDENTE,
                false,
                req.dataInicio(),
                req.dataFim()
        ));

        syncPendingUserAccess(vinculo, req);
        return updated;
    }

    public void deleteMorador(String email, Long id) {
        var scope = moradorScopeService.getScope(email);
        PessoaUnidade vinculo = pessoaUnidadeService.getEntity(id, scope.condominioId());
        assertCanManageUnit(email, vinculo.getUnidade().getId());

        if (!Boolean.TRUE.equals(vinculo.getEhMorador())) {
            throw new BadRequestException("Vinculo informado nao pertence a um morador.");
        }
        if (Boolean.TRUE.equals(vinculo.getPrincipal())) {
            throw new BadRequestException("Nao e permitido remover o morador principal por este fluxo.");
        }
        if (vinculo.getPessoa() != null && vinculo.getPessoa().getId().equals(scope.pessoaId())) {
            throw new BadRequestException("Nao e permitido remover o proprio vinculo.");
        }

        pessoaUnidadeService.delete(id, scope.condominioId());
    }

    public PessoaUnidadeResponse sendInvite(String email, Long id) {
        var scope = moradorScopeService.getScope(email);
        PessoaUnidade vinculo = pessoaUnidadeService.getEntity(id, scope.condominioId());
        assertCanManageUnit(email, vinculo.getUnidade().getId());

        if (!Boolean.TRUE.equals(vinculo.getEhMorador())) {
            throw new BadRequestException("Vinculo informado nao pertence a um morador.");
        }

        return pessoaUnidadeService.enviarConvite(id, scope.condominioId());
    }

    public List<Long> getManageableUnitIds(String email) {
        var usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ForbiddenException("Usuario nao encontrado"));

        return pessoaUnidadeRepository.findAllByUsuarioIdAndEhMoradorTrueAndAtivoTrue(usuario.getId())
                .stream()
                .filter(vinculo -> Boolean.TRUE.equals(vinculo.getPrincipal()))
                .map(vinculo -> vinculo.getUnidade().getId())
                .distinct()
                .sorted()
                .toList();
    }

    private void assertCanManageUnit(String email, Long unidadeId) {
        Set<Long> manageableUnitIds = getManageableUnitIds(email).stream().collect(Collectors.toSet());
        if (!manageableUnitIds.contains(unidadeId)) {
            throw new ForbiddenException("Apenas o morador principal pode gerenciar moradores desta unidade.");
        }
    }

    private void validateCreateRequest(MoradorManagedCreateRequest req) {
        if (req == null) {
            throw new BadRequestException("Body da requisicao e obrigatorio.");
        }
        if (req.unidadeId() == null) {
            throw new BadRequestException("unidadeId e obrigatorio.");
        }
        if (req.nome() == null || req.nome().isBlank()) {
            throw new BadRequestException("Nome e obrigatorio.");
        }
        if (req.cpfCnpj() == null || req.cpfCnpj().isBlank()) {
            throw new BadRequestException("cpfCnpj e obrigatorio.");
        }
    }

    private boolean hasActiveAccount(PessoaUnidade vinculo) {
        return vinculo.getUsuario() != null
                && Boolean.TRUE.equals(vinculo.getUsuario().getAtivo())
                && (vinculo.getConviteAceitoEm() != null || !Boolean.TRUE.equals(vinculo.getUsuario().getPrimeiroAcesso()));
    }

    private void syncPendingUserAccess(PessoaUnidade vinculo, MoradorManagedCreateRequest req) {
        if (vinculo.getUsuario() == null || !Boolean.TRUE.equals(vinculo.getUsuario().getPrimeiroAcesso())) {
            return;
        }

        String requestedEmail = normalizeEmail(req.email());
        if (requestedEmail != null && usuarioRepository.existsByEmail(requestedEmail) && !requestedEmail.equals(vinculo.getUsuario().getEmail())) {
            throw new BadRequestException("Ja existe um usuario cadastrado com este e-mail.");
        }

        vinculo.getUsuario().setNome(req.nome().trim());
        if (requestedEmail != null) {
            vinculo.getUsuario().setEmail(requestedEmail);
        }
        usuarioRepository.save(vinculo.getUsuario());
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase();
    }
}
