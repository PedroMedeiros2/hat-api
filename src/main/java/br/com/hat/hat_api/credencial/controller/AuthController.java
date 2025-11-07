package br.com.hat.hat_api.credencial.controller;

import br.com.hat.hat_api.config.security.JwtTokenProvider;
import br.com.hat.hat_api.credencial.dto.LoginRequest;
import br.com.hat.hat_api.credencial.dto.LoginResponse;
import br.com.hat.hat_api.credencial.model.Funcionarios;
import br.com.hat.hat_api.permissoes.dto.PermissaoSistemaDTO;
import br.com.hat.hat_api.permissoes.service.UsuarioPermissaoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UsuarioPermissaoService usuarioPermissaoService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.username(),
                            loginRequest.password()
                    )
            );

            String matricula = loginRequest.username();
            Object principal = authentication.getPrincipal();
            String nomeDoUsuario = ((Funcionarios) principal).getNome();

            List<PermissaoSistemaDTO> permissoesDTO = usuarioPermissaoService.getPermissoesSistema(matricula);

            List<String> codigosPermissao = permissoesDTO.stream()
                    .map(PermissaoSistemaDTO::getCodigo)
                    .collect(Collectors.toList());

            if (codigosPermissao.isEmpty()) {
                Map<String, Object> body = new HashMap<>();
                body.put("status", HttpStatus.UNAUTHORIZED.value());
                body.put("error", "Unauthorized");
                body.put("message", "Usuário não possui permissões de sistema para acesso.");
                body.put("errorCode", "AUTH_NO_PERMISSIONS");
                body.put("timestamp", System.currentTimeMillis());

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
            }

            List<GrantedAuthority> authorities = codigosPermissao.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            Authentication authCompleto = new UsernamePasswordAuthenticationToken(
                    principal,
                    authentication.getCredentials(),
                    authorities
            );
            SecurityContextHolder.getContext().setAuthentication(authCompleto);
            String jwt = tokenProvider.generateToken(authCompleto);

            return ResponseEntity.ok(new LoginResponse(jwt, nomeDoUsuario, codigosPermissao));

        } catch (BadCredentialsException e) {
            Map<String, Object> body = new HashMap<>();
            body.put("status", HttpStatus.UNAUTHORIZED.value());
            body.put("error", "Unauthorized");
            body.put("message", "Usuário ou senha inválidos.");
            body.put("errorCode", "AUTH_INVALID_CREDENTIALS");
            body.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);

        } catch (Exception e) {
            Map<String, Object> body = new HashMap<>();
            body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            ToH_Detection_User:
            body.put("error", "Internal Server Error");
            body.put("message", "Ocorreu um erro inesperado no servidor.");
            body.put("errorCode", "AUTH_INTERNAL_ERROR");
            body.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
        }
    }
}