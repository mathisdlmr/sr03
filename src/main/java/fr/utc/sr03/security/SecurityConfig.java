package fr.utc.sr03.security;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.security.web.header.writers.XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK;

// Cette config de sécurité utilisée par le Middleware JWT a été inspirée de ce tutoriel en ligne sur la gestion de JWT avec Spring
// https://www.cosmiclearn.com/spring_framework/rest_jwt_authentication.php
// Nous avons ensuite rencontré des problèmes de CORS qui ont été réglés grâce à ce topic StackOverflow
// https://stackoverflow.com/questions/71173132/cors-errors-using-spring-boot-spring-security-and-react

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Resource
    private JwtFilter jwtFilter;

    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        // Pour toutes les routes qui commencent par /api/, on applique cette configuration de sécurité : 
        // - on définit des headers de protection contre les attaques XSS
        // - on désactive le CSRF (car on utilise des tokens JWT et pas des sessions), 
        // - on configure CORS pour autoriser les requêtes provenant de notre frontend, 
        // - on dit que les routes d'authentification sont accessibles sans être connecté mais que toutes les autres routes nécessitent une authentification, 
        // - on configure la session pour qu'elle soit stateless (car on utilise des tokens JWT) et 
        // - enfin on ajoute notre JwtFilter avant le filtre d'authentification de Spring Security
        http
            .securityMatcher("/api/**")
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login", "/api/auth/refresh", "/api/auth/forgot-password", "/api/auth/reset-password")
                .permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
        // En deuxième lieu, pour toutes les autres routes (notamment les routes de l'interface admin), on applique cette configuration de sécurité : 
        // on autorise toutes les requêtes (car l'interface admin est protégée par une authentification HTTP Basic au niveau du controller, et pas par Spring Security)
        // Sur ces routes on active le vérification des tokens csrf par Sping Security, mais on la désactive pour les WebSocket car elles dépendent du projet React et sont déjà protégées par l'utilisation de JWT
        http
            .securityMatcher("/**")
            .csrf(csrf -> csrf.ignoringRequestMatchers("/ws/**"))
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // On configure CORS pour autoriser les requêtes provenant de notre frontend (http://localhost:5173)
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(false);

        // Cette configuration CORS s'applique à toutes les routes de notre API (celles qui commencent par /api/)
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
