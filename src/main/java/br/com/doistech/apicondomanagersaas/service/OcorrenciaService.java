package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.common.exception.ForbiddenException;
import br.com.doistech.apicondomanagersaas.common.exception.NotFoundException;
import br.com.doistech.apicondomanagersaas.config.JwtUtil;
import br.com.doistech.apicondomanagersaas.domain.ocorrencia.*;
import br.com.doistech.apicondomanagersaas.domain.usuario.Usuario;
import br.com.doistech.apicondomanagersaas.dto.ocorrencia.*;
import br.com.doistech.apicondomanagersaas.repository.OcorrenciaAnexoRepository;
import br.com.doistech.apicondomanagersaas.repository.OcorrenciaMensagemRepository;
import br.com.doistech.apicondomanagersaas.repository.OcorrenciaRepository;
import br.com.doistech.apicondomanagersaas.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OcorrenciaService {

    private final OcorrenciaRepository ocorrenciaRepository;
    private final OcorrenciaAnexoRepository anexoRepository;
    private final OcorrenciaMensagemRepository mensagemRepository;
    private final UsuarioRepository usuarioRepository;
    private final MoradorScopeService moradorScopeService;
    private final JwtUtil jwtUtil;
    private final HttpServletRequest request;

    private Long currentUid() {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ForbiddenException("Token ausente");
        }
        String token = authHeader.substring(7);
        var jws = jwtUtil.parse(token);
        Object claim = jws.getPayload().get("uid");
        if (claim instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(claim));
    }

    private boolean hasAuthority(String authority) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> authority.equals(a.getAuthority()));
    }

    private String currentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
            throw new ForbiddenException("Usuário autenticado não identificado");
        }
        return auth.getName();
    }

    private OcorrenciaAutorTipo currentAutorTipo() {
        if (hasAuthority("ROLE_ADMIN_CONDOMINIO")) {
            return OcorrenciaAutorTipo.admin_condominio;
        }
        return OcorrenciaAutorTipo.morador;
    }

    private OcorrenciaArquivoTipo resolveArquivoTipo(String contentType, String fileName) {
        String normalizedContentType = contentType == null ? "" : contentType.toLowerCase();
        String normalizedName = fileName == null ? "" : fileName.toLowerCase();
        if (normalizedContentType.startsWith("video/") || normalizedName.endsWith(".mp4")
                || normalizedName.endsWith(".mov") || normalizedName.endsWith(".webm")) {
            return OcorrenciaArquivoTipo.video;
        }
        return OcorrenciaArquivoTipo.imagem;
    }

    private List<Ocorrencia> filterOcorrencias(
            List<Ocorrencia> ocorrencias,
            OcorrenciaStatus status,
            OcorrenciaCategoria categoria
    ) {
        return ocorrencias.stream()
                .filter(ocorrencia -> status == null || ocorrencia.getStatus() == status)
                .filter(ocorrencia -> categoria == null || ocorrencia.getCategoria() == categoria)
                .toList();
    }

    private OcorrenciaSummaryResponse mapSummary(Ocorrencia ocorrencia) {
        return new OcorrenciaSummaryResponse(
                ocorrencia.getId(),
                ocorrencia.getCodigo(),
                ocorrencia.getCondominio().getId(),
                ocorrencia.getMoradorVinculo().getId(),
                ocorrencia.getUnidade().getId(),
                ocorrencia.getCategoria(),
                ocorrencia.getTitulo(),
                ocorrencia.getDescricao(),
                ocorrencia.getLocalOcorrencia(),
                ocorrencia.getStatus(),
                ocorrencia.getMoradorVinculo().getPessoa().getNome(),
                ocorrencia.getUnidade().getIdentificacao(),
                (int) anexoRepository.countByOcorrenciaId(ocorrencia.getId()),
                (int) mensagemRepository.countByOcorrenciaId(ocorrencia.getId()),
                ocorrencia.getCreatedAt(),
                ocorrencia.getUpdatedAt(),
                ocorrencia.getResolvidaEm()
        );
    }

    private OcorrenciaDetailResponse mapDetail(Ocorrencia ocorrencia) {
        List<OcorrenciaAnexoResponse> anexos = anexoRepository.findAllByOcorrenciaIdOrderByCreatedAtAsc(ocorrencia.getId())
                .stream()
                .map(anexo -> new OcorrenciaAnexoResponse(
                        anexo.getId(),
                        anexo.getArquivoUrl(),
                        anexo.getArquivoNome(),
                        anexo.getContentType(),
                        anexo.getTipoArquivo(),
                        anexo.getTamanhoBytes(),
                        anexo.getCreatedAt()
                ))
                .toList();

        List<OcorrenciaMensagemResponse> mensagens = mensagemRepository.findAllByOcorrenciaIdOrderByCreatedAtAsc(ocorrencia.getId())
                .stream()
                .map(mensagem -> new OcorrenciaMensagemResponse(
                        mensagem.getId(),
                        mensagem.getAutor().getId(),
                        mensagem.getAutorTipo(),
                        mensagem.getAutor().getNome(),
                        mensagem.getMensagem(),
                        mensagem.getCreatedAt()
                ))
                .toList();

        return new OcorrenciaDetailResponse(
                ocorrencia.getId(),
                ocorrencia.getCodigo(),
                ocorrencia.getCondominio().getId(),
                ocorrencia.getMoradorVinculo().getId(),
                ocorrencia.getUnidade().getId(),
                ocorrencia.getCategoria(),
                ocorrencia.getTitulo(),
                ocorrencia.getDescricao(),
                ocorrencia.getLocalOcorrencia(),
                ocorrencia.getStatus(),
                ocorrencia.getMoradorVinculo().getPessoa().getNome(),
                ocorrencia.getUnidade().getIdentificacao(),
                anexos,
                mensagens,
                ocorrencia.getCreatedAt(),
                ocorrencia.getUpdatedAt(),
                ocorrencia.getResolvidaEm()
        );
    }

    private Ocorrencia getOcorrenciaAdmin(Long condominioId, Long id) {
        return ocorrenciaRepository.findByIdAndCondominioId(id, condominioId)
                .orElseThrow(() -> new NotFoundException("Ocorrência não encontrada"));
    }

    private Ocorrencia getOcorrenciaMorador(String email, Long id) {
        var scope = moradorScopeService.getScope(email);
        Ocorrencia ocorrencia = ocorrenciaRepository.findByIdAndCondominioId(id, scope.condominioId())
                .orElseThrow(() -> new NotFoundException("Ocorrência não encontrada"));
        Long uid = currentUid();
        if (ocorrencia.getMoradorVinculo().getUsuario() == null
                || !ocorrencia.getMoradorVinculo().getUsuario().getId().equals(uid)) {
            throw new ForbiddenException("Acesso negado");
        }
        return ocorrencia;
    }

    private Usuario getCurrentUsuario() {
        Long uid = currentUid();
        return usuarioRepository.findById(uid)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
    }

    @Transactional(readOnly = true)
    public List<OcorrenciaSummaryResponse> listAdmin(
            Long condominioId,
            OcorrenciaStatus status,
            OcorrenciaCategoria categoria
    ) {
        return filterOcorrencias(
                ocorrenciaRepository.findAllByCondominioIdOrderByUpdatedAtDesc(condominioId),
                status,
                categoria
        ).stream().map(this::mapSummary).toList();
    }

    @Transactional(readOnly = true)
    public OcorrenciaDetailResponse getAdmin(Long condominioId, Long id) {
        return mapDetail(getOcorrenciaAdmin(condominioId, id));
    }

    @Transactional(readOnly = true)
    public List<OcorrenciaSummaryResponse> listMorador(
            String email,
            OcorrenciaStatus status,
            OcorrenciaCategoria categoria
    ) {
        var scope = moradorScopeService.getScope(email);
        return filterOcorrencias(
                ocorrenciaRepository.findAllByCondominioIdAndMoradorVinculoUsuarioIdOrderByUpdatedAtDesc(
                        scope.condominioId(),
                        currentUid()
                ),
                status,
                categoria
        ).stream().map(this::mapSummary).toList();
    }

    @Transactional(readOnly = true)
    public OcorrenciaDetailResponse getMorador(String email, Long id) {
        return mapDetail(getOcorrenciaMorador(email, id));
    }

    @Transactional
    public OcorrenciaDetailResponse createMorador(String email, OcorrenciaCreateRequest req) {
        var vinculo = moradorScopeService.getPrincipalPessoaUnidade(email);
        LocalDateTime now = LocalDateTime.now();

        Ocorrencia ocorrencia = Ocorrencia.builder()
                .condominio(vinculo.getCondominio())
                .moradorVinculo(vinculo)
                .unidade(vinculo.getUnidade())
                .codigo("PENDENTE")
                .categoria(req.categoria())
                .titulo(req.titulo().trim())
                .descricao(req.descricao().trim())
                .localOcorrencia(req.localOcorrencia() == null ? null : req.localOcorrencia().trim())
                .status(OcorrenciaStatus.aberta)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Ocorrencia saved = ocorrenciaRepository.save(ocorrencia);
        saved.setCodigo(generateCodigo(saved.getId()));
        saved = ocorrenciaRepository.save(saved);

        if (req.anexos() != null) {
            for (OcorrenciaAnexoCreateRequest anexo : req.anexos()) {
                anexoRepository.save(OcorrenciaAnexo.builder()
                        .ocorrencia(saved)
                        .arquivoUrl(anexo.arquivoUrl())
                        .arquivoNome(anexo.arquivoNome())
                        .contentType(anexo.contentType())
                        .tipoArquivo(resolveArquivoTipo(anexo.contentType(), anexo.arquivoNome()))
                        .tamanhoBytes(anexo.tamanhoBytes())
                        .createdAt(now)
                        .build());
            }
        }

        return mapDetail(saved);
    }

    private String generateCodigo(Long id) {
        return "OCR-" + String.format("%06d", id);
    }

    @Transactional
    public OcorrenciaMensagemResponse addMensagemAdmin(
            Long condominioId,
            Long ocorrenciaId,
            OcorrenciaMensagemCreateRequest req
    ) {
        if (!hasAuthority("ROLE_ADMIN_CONDOMINIO")) {
            throw new ForbiddenException("Acesso negado");
        }

        Ocorrencia ocorrencia = getOcorrenciaAdmin(condominioId, ocorrenciaId);
        Usuario autor = getCurrentUsuario();
        LocalDateTime now = LocalDateTime.now();

        OcorrenciaMensagem mensagem = mensagemRepository.save(OcorrenciaMensagem.builder()
                .ocorrencia(ocorrencia)
                .autor(autor)
                .autorTipo(currentAutorTipo())
                .mensagem(req.mensagem().trim())
                .createdAt(now)
                .build());

        if (ocorrencia.getStatus() == OcorrenciaStatus.aberta
                || ocorrencia.getStatus() == OcorrenciaStatus.em_analise
                || ocorrencia.getStatus() == OcorrenciaStatus.aguardando_morador) {
            ocorrencia.setStatus(OcorrenciaStatus.respondida);
        }
        ocorrencia.setUpdatedAt(now);
        ocorrenciaRepository.save(ocorrencia);

        return new OcorrenciaMensagemResponse(
                mensagem.getId(),
                autor.getId(),
                mensagem.getAutorTipo(),
                autor.getNome(),
                mensagem.getMensagem(),
                mensagem.getCreatedAt()
        );
    }

    @Transactional
    public OcorrenciaMensagemResponse addMensagemMorador(
            String email,
            Long ocorrenciaId,
            OcorrenciaMensagemCreateRequest req
    ) {
        Ocorrencia ocorrencia = getOcorrenciaMorador(email, ocorrenciaId);
        Usuario autor = getCurrentUsuario();
        LocalDateTime now = LocalDateTime.now();

        OcorrenciaMensagem mensagem = mensagemRepository.save(OcorrenciaMensagem.builder()
                .ocorrencia(ocorrencia)
                .autor(autor)
                .autorTipo(currentAutorTipo())
                .mensagem(req.mensagem().trim())
                .createdAt(now)
                .build());

        if (ocorrencia.getStatus() == OcorrenciaStatus.aguardando_morador) {
            ocorrencia.setStatus(OcorrenciaStatus.aberta);
        }
        ocorrencia.setUpdatedAt(now);
        ocorrenciaRepository.save(ocorrencia);

        return new OcorrenciaMensagemResponse(
                mensagem.getId(),
                autor.getId(),
                mensagem.getAutorTipo(),
                autor.getNome(),
                mensagem.getMensagem(),
                mensagem.getCreatedAt()
        );
    }

    @Transactional
    public OcorrenciaDetailResponse updateStatusAdmin(
            Long condominioId,
            Long ocorrenciaId,
            OcorrenciaStatusUpdateRequest req
    ) {
        if (!hasAuthority("ROLE_ADMIN_CONDOMINIO")) {
            throw new ForbiddenException("Acesso negado");
        }

        Ocorrencia ocorrencia = getOcorrenciaAdmin(condominioId, ocorrenciaId);
        LocalDateTime now = LocalDateTime.now();
        ocorrencia.setStatus(req.status());
        ocorrencia.setUpdatedAt(now);
        ocorrencia.setResolvidaEm(req.status() == OcorrenciaStatus.resolvida ? now : null);
        return mapDetail(ocorrenciaRepository.save(ocorrencia));
    }
}
