package com.prova.pf.security;

import java.util.Collection;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    static final String ROLES_CLAIM = "https://artigos.example.com/roles";
    public static final String EMAIL_CLAIM = "https://artigos.example.com/email";

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Object roles = jwt.getClaims().get(ROLES_CLAIM);
            if (roles instanceof Collection<?> values) {
                return values.stream()
                    .filter(role -> "ADMIN".equals(role) || "USER".equals(role))
                    .<GrantedAuthority>map(role ->
                        new SimpleGrantedAuthority("ROLE_" + role))
                    .toList();
            }
            return List.of();
        });

        return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(requests -> requests
                .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                .requestMatchers(HttpMethod.POST, "/artigos").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/artigos/{id}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/artigos").hasAnyRole("ADMIN", "USER")
                .anyRequest().denyAll())
            .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter)))
            .build();
    }
}
