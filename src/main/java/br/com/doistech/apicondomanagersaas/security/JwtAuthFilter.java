package br.com.doistech.apicondomanagersaas.security;

import br.com.doistech.apicondomanagersaas.config.JwtUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, java.io.IOException {

        String auth = normalizeAuthorizationHeader(request.getHeader("Authorization"));

        if (hasBearerToken(auth)) {
            String token = auth.substring(7);

            try {
                var jws = jwtUtil.parse(token);
                String email = jws.getPayload().getSubject();

                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                    var authentication = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }

            } catch (Exception ex) {
                log.warn("Falha ao autenticar JWT em {} {}: {}",
                        request.getMethod(),
                        request.getRequestURI(),
                        ex.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private String normalizeAuthorizationHeader(String authHeader) {
        return authHeader == null ? null : authHeader.trim();
    }

    private boolean hasBearerToken(String authHeader) {
        return authHeader != null && authHeader.regionMatches(true, 0, "Bearer ", 0, 7);
    }
}
