package br.com.hat.hat_api.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
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

        String token = resolveToken(request);
        String username = null;

        if (token != null) {
            try {
                username = jwtTokenProvider.getUsernameFromToken(token);

            } catch (ExpiredJwtException e) {
                logger.warn("JWT token expirado: {}", e);
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Token expirado. Por favor, faça login novamente.");
                return;

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
                logger.error("JWT claims vazias: {}", e);
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Token JWT inválido (claims vazias).");
                return;
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            if (jwtTokenProvider.validateToken(token, userDetails)) {
                List<String> perms = jwtTokenProvider.getPermissionsFromToken(token);

                List<SimpleGrantedAuthority> authorities = perms.stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }

   private String resolveToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("auth-token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new HashMap<>();
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("timestamp", System.currentTimeMillis());

        response.getOutputStream().write(objectMapper.writeValueAsBytes(body));
    }
}
