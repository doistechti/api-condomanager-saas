package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.common.exception.ForbiddenException;
import br.com.doistech.apicondomanagersaas.common.exception.NotFoundException;
import br.com.doistech.apicondomanagersaas.config.JwtUtil;
import br.com.doistech.apicondomanagersaas.domain.chat.*;
import br.com.doistech.apicondomanagersaas.domain.usuario.Usuario;
import br.com.doistech.apicondomanagersaas.dto.chat.*;
import br.com.doistech.apicondomanagersaas.repository.*;
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
public class ChatService {

    private final ConversaRepository conversaRepository;
    private final MensagemRepository mensagemRepository;
    private final CondominioService condominioService;
    private final PessoaUnidadeRepository pessoaUnidadeRepository;
    private final UsuarioRepository usuarioRepository;
    private final JwtUtil jwtUtil;
    private final HttpServletRequest request;

    // ===== Helpers (uid + roles via token) =====
    private Long currentUid() {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ForbiddenException("Token ausente");
        }
        String token = authHeader.substring(7);
        var jws = jwtUtil.parse(token);
        Object claim = jws.getPayload().get("uid");
        if (claim instanceof Number n) return n.longValue();
        return Long.parseLong(String.valueOf(claim));
    }

    private boolean hasAuthority(String authority) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> authority.equals(a.getAuthority()));
    }

    private RemetenteTipo currentRemetenteTipo() {
        if (hasAuthority("ROLE_ADMIN_SAAS")) return RemetenteTipo.admin_saas;
        if (hasAuthority("ROLE_ADMIN_CONDOMINIO")) return RemetenteTipo.admin_condominio;
        return RemetenteTipo.morador;
    }

    private void assertCanAccessConversa(Long condominioId, Conversa conversa) {
        // tenant já é validado pelo TenantIsolationFilter, mas aqui garantimos conversa pertence ao condominioId
        if (!conversa.getCondominio().getId().equals(condominioId)) {
            throw new ForbiddenException("Acesso negado");
        }

        // MORADOR só pode acessar conversa do próprio vinculo (usuario_id)
        if (hasAuthority("ROLE_MORADOR")) {
            Long uid = currentUid();
            if (conversa.getMoradorVinculo() == null || conversa.getMoradorVinculo().getUsuario() == null) {
                throw new ForbiddenException("Acesso negado");
            }
            if (!conversa.getMoradorVinculo().getUsuario().getId().equals(uid)) {
                throw new ForbiddenException("Acesso negado");
            }
        }
    }

    // ===== Conversas =====
    public List<ConversaResponse> listConversas(Long condominioId, ConversaTipo tipo, ConversaStatus status) {
        Long uid = currentUid();

        List<Conversa> conversas = (tipo != null && status != null)
                ? conversaRepository.findAllByCondominioIdAndTipoAndStatusOrderByUltimaMensagemAtDesc(condominioId, tipo, status)
                : (tipo != null)
                ? conversaRepository.findAllByCondominioIdAndTipoOrderByUltimaMensagemAtDesc(condominioId, tipo)
                : (status != null)
                ? conversaRepository.findAllByCondominioIdAndStatusOrderByUltimaMensagemAtDesc(condominioId, status)
                : conversaRepository.findAllByCondominioIdOrderByUltimaMensagemAtDesc(condominioId);

        // MORADOR: filtra só conversas do seu vinculo
        if (hasAuthority("ROLE_MORADOR")) {
            conversas = conversas.stream()
                    .filter(c -> c.getMoradorVinculo() != null && c.getMoradorVinculo().getUsuario() != null
                            && c.getMoradorVinculo().getUsuario().getId().equals(uid))
                    .toList();
        }

        return conversas.stream().map(c -> {
            long unread = mensagemRepository.countByConversaIdAndLidaFalseAndRemetenteIdNot(c.getId(), uid);

            String condNome = null;
            try { condNome = c.getCondominio().getNome(); } catch (Exception ignored) {}

            String moradorNome = null;
            Long moradorId = null;
            if (c.getMoradorVinculo() != null) {
                moradorId = c.getMoradorVinculo().getId();
                try { moradorNome = c.getMoradorVinculo().getPessoa().getNome(); } catch (Exception ignored) {}
            }

            return new ConversaResponse(
                    c.getId(),
                    c.getTipo(),
                    c.getCondominio().getId(),
                    moradorId,
                    c.getTitulo(),
                    c.getStatus(),
                    c.getCreatedAt(),
                    c.getUpdatedAt(),
                    c.getUltimaMensagemAt(),
                    condNome,
                    moradorNome,
                    unread
            );
        }).toList();
    }

    public ConversaResponse createConversa(Long condominioId, ConversaCreateRequest req) {
        // ADMIN_CONDOMINIO pode iniciar conversa com um morador específico
        // MORADOR pode iniciar conversa, mas moradorId é resolvido pelo seu vínculo (evita spoofing)
        Long uid = currentUid();

        Long moradorVinculoId = req.moradorId();

        if (hasAuthority("ROLE_MORADOR")) {
            var vinculos = pessoaUnidadeRepository.findAllByUsuarioIdAndEhMoradorTrueAndAtivoTrue(uid);
            if (vinculos.isEmpty()) throw new ForbiddenException("Usuário morador sem vínculo ativo");
            moradorVinculoId = vinculos.get(0).getId();
        }

        var condominio = condominioService.getEntity(condominioId);

        var conversa = Conversa.builder()
                .condominio(condominio)
                .tipo(req.tipo())
                .status(ConversaStatus.aberta)
                .titulo(req.titulo())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .ultimaMensagemAt(LocalDateTime.now())
                .build();

        if (moradorVinculoId != null) {
            var moradorVinculo = pessoaUnidadeRepository.findByIdAndCondominioId(moradorVinculoId, condominioId)
                    .orElseThrow(() -> new NotFoundException("Morador não encontrado"));
            conversa.setMoradorVinculo(moradorVinculo);
        }

        var saved = conversaRepository.save(conversa);

        // Response
        String moradorNome = saved.getMoradorVinculo() != null ? saved.getMoradorVinculo().getPessoa().getNome() : null;
        return new ConversaResponse(
                saved.getId(),
                saved.getTipo(),
                condominioId,
                saved.getMoradorVinculo() != null ? saved.getMoradorVinculo().getId() : null,
                saved.getTitulo(),
                saved.getStatus(),
                saved.getCreatedAt(),
                saved.getUpdatedAt(),
                saved.getUltimaMensagemAt(),
                condominio.getNome(),
                moradorNome,
                0
        );
    }

    @Transactional
    public void fecharConversa(Long condominioId, Long conversaId) {
        Conversa c = conversaRepository.findByIdAndCondominioId(conversaId, condominioId)
                .orElseThrow(() -> new NotFoundException("Conversa não encontrada"));

        // MORADOR não pode fechar (regra do seu ChatWindow: somente não-morador)
        if (hasAuthority("ROLE_MORADOR")) {
            throw new ForbiddenException("Morador não pode encerrar conversa");
        }

        c.setStatus(ConversaStatus.fechada);
        c.setUpdatedAt(LocalDateTime.now());
        conversaRepository.save(c);
    }

    public UnreadCountResponse unreadCount(Long condominioId, ConversaTipo tipo) {
        Long uid = currentUid();
        long count = mensagemRepository.countConversasComNaoLidas(condominioId, tipo, uid);
        return new UnreadCountResponse(count);
    }

    // ===== Mensagens =====
    public List<MensagemResponse> listMensagens(Long condominioId, Long conversaId) {
        Conversa conversa = conversaRepository.findByIdAndCondominioId(conversaId, condominioId)
                .orElseThrow(() -> new NotFoundException("Conversa não encontrada"));

        assertCanAccessConversa(condominioId, conversa);

        return mensagemRepository.findAllByConversaIdOrderByCreatedAtAsc(conversaId).stream()
                .map(m -> new MensagemResponse(
                        m.getId(),
                        conversaId,
                        m.getRemetente().getId(),
                        m.getRemetenteTipo(),
                        m.getConteudo(),
                        Boolean.TRUE.equals(m.getLida()),
                        m.getCreatedAt()
                )).toList();
    }

    @Transactional
    public MensagemResponse sendMensagem(Long condominioId, Long conversaId, MensagemCreateRequest req) {
        Conversa conversa = conversaRepository.findByIdAndCondominioId(conversaId, condominioId)
                .orElseThrow(() -> new NotFoundException("Conversa não encontrada"));

        assertCanAccessConversa(condominioId, conversa);

        if (conversa.getStatus() == ConversaStatus.fechada) {
            throw new ForbiddenException("Conversa está encerrada");
        }

        Long uid = currentUid();
        Usuario remetente = usuarioRepository.findById(uid)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        Mensagem msg = Mensagem.builder()
                .conversa(conversa)
                .remetente(remetente)
                .remetenteTipo(currentRemetenteTipo())
                .conteudo(req.conteudo())
                .lida(false)
                .createdAt(LocalDateTime.now())
                .build();

        Mensagem saved = mensagemRepository.save(msg);

        conversa.setUltimaMensagemAt(saved.getCreatedAt());
        conversa.setUpdatedAt(LocalDateTime.now());
        conversaRepository.save(conversa);

        return new MensagemResponse(
                saved.getId(),
                conversaId,
                remetente.getId(),
                saved.getRemetenteTipo(),
                saved.getConteudo(),
                saved.getLida(),
                saved.getCreatedAt()
        );
    }

    @Transactional
    public void markAsRead(Long condominioId, Long conversaId) {
        Conversa conversa = conversaRepository.findByIdAndCondominioId(conversaId, condominioId)
                .orElseThrow(() -> new NotFoundException("Conversa não encontrada"));

        assertCanAccessConversa(condominioId, conversa);

        Long uid = currentUid();
        mensagemRepository.markAsRead(conversaId, uid);
    }
}