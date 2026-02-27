package br.com.doistech.apicondomanagersaas.config;

import br.com.doistech.apicondomanagersaas.security.JwtAuthFilter;
import br.com.doistech.apicondomanagersaas.security.TenantIsolationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final TenantIsolationFilter tenantIsolationFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, TenantIsolationFilter tenantIsolationFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.tenantIsolationFilter = tenantIsolationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/api/v1/public/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api-docs/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                // 1) autentica (SecurityContext)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                // 2) valida tenant (condominioId do path x claim)
                .addFilterAfter(tenantIsolationFilter, JwtAuthFilter.class);

        return http.build();
    }
}