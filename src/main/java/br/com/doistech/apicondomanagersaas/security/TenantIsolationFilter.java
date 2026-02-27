package br.com.doistech.apicondomanagersaas.security;

import br.com.doistech.apicondomanagersaas.common.web.RequestContext;
import br.com.doistech.apicondomanagersaas.config.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ✅ Multi-tenant (isolamento simples):
 *
 * Para endpoints que usam o padrão:
 *   /api/v1/condominios/{condominioId}/...
 *
 * - Se o usuário tiver ROLE_ADMIN_SAAS: libera (pode gerenciar múltiplos condomínios)
 * - Caso contrário: o {condominioId} do path PRECISA bater com o claim condominioId do JWT
 *
 * Isso evita que um ADMIN_CONDOMINIO acesse dados de outro condomínio apenas trocando o ID na URL.
 */
@Component
public class TenantIsolationFilter extends OncePerRequestFilter {

    private static final Pattern CONDOMINIO_PATH = Pattern.compile("^/api/v1/condominios/(\\d+)(/.*)?$");

    private final JwtUtil jwtUtil;

    public TenantIsolationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        Matcher m = CONDOMINIO_PATH.matcher(path);

        // Não é endpoint multi-tenant -> segue normal
        if (!m.matches()) {
            filterChain.doFilter(request, response);
            return;
        }

        Long pathCondominioId = null;
        try {
            pathCondominioId = Long.parseLong(m.group(1));
        } catch (NumberFormatException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Disponibiliza para camadas abaixo (se quiser usar no service no futuro)
        RequestContext.setCondominioId(pathCondominioId);

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            // Se ainda não autenticou, deixa o SecurityConfig retornar 401
            if (auth == null || !auth.isAuthenticated()) {
                filterChain.doFilter(request, response);
                return;
            }

            // ADMIN_SAAS pode acessar qualquer condomínio
            boolean isAdminSaas = auth.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_ADMIN_SAAS".equals(a.getAuthority()));

            if (isAdminSaas) {
                filterChain.doFilter(request, response);
                return;
            }

            // Para os demais perfis, valida claim condominioId
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                // Sem token: deixa o SecurityConfig bloquear
                filterChain.doFilter(request, response);
                return;
            }

            String token = authHeader.substring(7);
            var jws = jwtUtil.parse(token);
            Object claim = jws.getPayload().get("condominioId");

            if (claim == null) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            Long tokenCondominioId;
            if (claim instanceof Number n) {
                tokenCondominioId = n.longValue();
            } else {
                try {
                    tokenCondominioId = Long.parseLong(String.valueOf(claim));
                } catch (NumberFormatException ex) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            }

            if (!pathCondominioId.equals(tokenCondominioId)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            filterChain.doFilter(request, response);

        } catch (Exception ex) {
            // Token inválido/expirado: deixa o SecurityConfig bloquear com 401
            filterChain.doFilter(request, response);

        } finally {
            RequestContext.clear();
        }
    }
}