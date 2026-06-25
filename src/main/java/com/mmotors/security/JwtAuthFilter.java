package com.mmotors.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        String uri = request.getRequestURI();

        if (header == null || !header.startsWith("Bearer ")) {
            log.warn("[JWT] Pas de token pour {} {}", request.getMethod(), uri);
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            String email = jwtService.extractSubject(token);
            log.info("[JWT] Token reçu pour {} {} — email: {}", request.getMethod(), uri, email);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                if (jwtService.isTokenValid(token, userDetails)) {
                    var auth = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    log.info("[JWT] Auth OK pour {} — rôles: {}", email, userDetails.getAuthorities());
                } else {
                    log.warn("[JWT] Token invalide pour {}", email);
                }
            }
        } catch (Exception e) {
            log.error("[JWT] Erreur pour {} {}: {} — {}", request.getMethod(), uri,
                    e.getClass().getSimpleName(), e.getMessage());
        }

        chain.doFilter(request, response);
    }
}
