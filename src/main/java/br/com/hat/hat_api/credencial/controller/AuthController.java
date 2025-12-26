package br.com.hat.hat_api.credencial.controller;

import br.com.hat.hat_api.config.security.JwtTokenProvider;
import br.com.hat.hat_api.credencial.dto.LoginRequest;
import br.com.hat.hat_api.credencial.dto.LoginResponse;
import br.com.hat.hat_api.credencial.model.Funcionarios;
import br.com.hat.hat_api.credencial.repository.FuncionarioRepository;
import br.com.hat.hat_api.permissoes.dto.PermissaoSistemaDTO;
import br.com.hat.hat_api.permissoes.model.RefreshToken;
import br.com.hat.hat_api.permissoes.service.RefreshTokenService;
import br.com.hat.hat_api.permissoes.service.UsuarioPermissaoService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UsuarioPermissaoService usuarioPermissaoService;
    private final RefreshTokenService refreshTokenService;
    private final FuncionarioRepository funcionarioRepository;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider tokenProvider,
                          UsuarioPermissaoService usuarioPermissaoService,
                          RefreshTokenService refreshTokenService,
                          FuncionarioRepository funcionarioRepository) {

        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.usuarioPermissaoService = usuarioPermissaoService;
        this.refreshTokenService = refreshTokenService;
        this.funcionarioRepository = funcionarioRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        String matricula = loginRequest.username();

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(matricula, loginRequest.password())
            );

            Funcionarios funcionario = (Funcionarios) authentication.getPrincipal();

            List<String> codigosPermissao = usuarioPermissaoService.getPermissoesSistema(matricula)
                    .stream()
                    .map(PermissaoSistemaDTO::getCodigo)
                    .collect(Collectors.toList());

            if (codigosPermissao.isEmpty()) {
                Map<String, Object> body = new HashMap<>();
                body.put("status", HttpStatus.FORBIDDEN.value());
                body.put("error", "Forbidden");
                body.put("message", "Acesso negado. Você não possui permissão para este recurso.");
                body.put("errorCode", "AUTH_NO_PERMISSIONS");
                body.put("timestamp", System.currentTimeMillis());

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
            }

            List<GrantedAuthority> authorities = codigosPermissao.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            Authentication authCompleto = new UsernamePasswordAuthenticationToken(funcionario, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authCompleto);

            String jwtAccessToken = tokenProvider.generateToken(authCompleto);

            ResponseCookie accessTokenCookie = ResponseCookie.from("auth-token", jwtAccessToken)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(tokenProvider.getExpirationMs() / 1000)
                    .build();

            RefreshToken refreshToken = refreshTokenService.createRefreshToken(matricula);
            long refreshExpiration = (refreshToken.getDataExpiracao().toEpochMilli() - Instant.now().toEpochMilli()) / 1000;

            ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh-token", refreshToken.getToken())
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(refreshExpiration)
                    .build();

            LoginResponse response = new LoginResponse(
                    funcionario.getMatricula(),
                    funcionario.getNome(),
                    codigosPermissao,
                    tokenProvider.getExpirationDateFromToken(jwtAccessToken).getTime()
            );

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                    .body(response);

        } catch (BadCredentialsException | UsernameNotFoundException | InternalAuthenticationServiceException ex) {

            Map<String, Object> body = new HashMap<>();
            body.put("status", HttpStatus.UNAUTHORIZED.value());
            body.put("error", "Unauthorized");
            body.put("message", "Matrícula ou senha incorretas.");
            body.put("errorCode", "AUTH_INVALID_CREDENTIALS");
            body.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(HttpServletRequest request) {
        String requestRefreshToken = null;

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refresh-token".equals(cookie.getName())) {
                    requestRefreshToken = cookie.getValue();
                    break;
                }
            }
        }

        RefreshToken refreshToken = refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .orElseThrow();

        String matricula = refreshToken.getMatriculaUsuario();

        Funcionarios funcionario = funcionarioRepository
                .findByMatriculaAndStatus(matricula, "A")
                .orElseThrow(() -> new UsernameNotFoundException("Usuário inativo ou não encontrado ao renovar token."));

        List<String> codigosPermissao = usuarioPermissaoService.getPermissoesSistema(matricula)
                .stream()
                .map(PermissaoSistemaDTO::getCodigo)
                .collect(Collectors.toList());

        List<GrantedAuthority> authorities = codigosPermissao.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        Authentication authCompleto = new UsernamePasswordAuthenticationToken(funcionario, null, authorities);

        String newAccessToken = tokenProvider.generateToken(authCompleto);

        ResponseCookie accessTokenCookie = ResponseCookie.from("auth-token", newAccessToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(tokenProvider.getExpirationMs() / 1000)
                .build();

        LoginResponse response = new LoginResponse(
                funcionario.getMatricula(),
                funcionario.getNome(),
                codigosPermissao,
                tokenProvider.getExpirationDateFromToken(newAccessToken).getTime()
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .body(response);
    }
}
