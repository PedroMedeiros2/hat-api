package br.com.hat.hat_api.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                username = jwtTokenProvider.getUsernameFromToken(token);

            } catch (ExpiredJwtException e) {
                logger.warn("JWT token expirado: {}", e);
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Token expirado. Por favor, faça login novamente.");
                return; // Interrompe a execução

            } catch (SignatureException e) {
                logger.error("JWT assinatura inválida: {}", e);
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Token JWT inválido (assinatura).");
                return;

            } catch (MalformedJwtException e) {
                logger.error("JWT token malformado: {}", e);
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Token JWT malformado.");
                return;

            } catch (UnsupportedJwtException e) {
                logger.error("JWT token não suportado: {}", e);
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Token JWT não suportado.");
                return;

            } catch (IllegalArgumentException e) {
                logger.error("JWT claims estão vazias: {}", e);
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Token JWT inválido (claims vazias).");
                return;
            }
        }

        // Se passou por tudo e tem um usuário, continua a validação
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            if (jwtTokenProvider.validateToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }


    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new HashMap<>();
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("timestamp", System.currentTimeMillis());

        // Escreve o JSON no corpo da resposta
        response.getOutputStream().write(objectMapper.writeValueAsBytes(body));
    }
}