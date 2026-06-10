package fr.utc.sr03.security;

import fr.utc.sr03.model.Users;
import fr.utc.sr03.services.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// Ce Middleware JWT a été inspiré de ce tutoriel en ligne sur la gestion de JWT avec Spring
// https://www.cosmiclearn.com/spring_framework/rest_jwt_authentication.php

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // On extrait le token JWT depuis le header "Authorization" de la requête
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            // On n'authentifie qu'à partir des access tokens
            if (jwtUtil.isTokenValid(token) && !jwtUtil.isRefreshToken(token)) {

                // On récupère les informations de l'utilisateur à partir du token JWT
                String email = jwtUtil.extractEmail(token);
                Users user = userService.getUserByEmailAddress(email);

                // Puis on fait quelques vérifications avant de le connecter
                // La vérification `SecurityContextHolder.getContext().getAuthentication() == null` permet de s'assurer qu'on n'écrase pas une authentification déjà présente
                if (user != null && user.isActive() && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null, List.of());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
