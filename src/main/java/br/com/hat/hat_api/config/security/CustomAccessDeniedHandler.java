package br.com.hat.hat_api.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json");

        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("error", "Forbidden");
        body.put("message", "Acesso negado. Você não possui permissão para este recurso.");
        body.put("timestamp", System.currentTimeMillis());

        String path = request.getRequestURI();
        if (path.contains("/indicador/")) {
            body.put("errorCode", "INDICADOR_ACCESS_DENIED");
        } else {
            body.put("errorCode", "AUTH_NO_PERMISSIONS");
        }

        new ObjectMapper().writeValue(response.getOutputStream(), body);
    }

}
