package br.com.hat.hat_api.credencial.controller;

import br.com.hat.hat_api.permissoes.dto.AtualizarPermissoesRequest;
import br.com.hat.hat_api.credencial.dto.FuncionarioResponseDTO;
import br.com.hat.hat_api.credencial.service.FuncionarioService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class FuncionarioController {

    private static final Logger logger = LoggerFactory.getLogger(FuncionarioController.class);

    private final FuncionarioService service;


    @GetMapping
    public ResponseEntity<Object> mostrar(@RequestParam String valor) {

        Object principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        String matriculaLogado;
        if (principal instanceof UserDetails userDetails) {
            matriculaLogado = userDetails.getUsername();
        } else {
            matriculaLogado = principal.toString();
        }

        List<String> permissoes = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        boolean temPermissao = permissoes.contains("GERENCIAR_PERMIS");

        if (!temPermissao) {
            Map<String, Object> body = new HashMap<>();
            body.put("status", HttpStatus.FORBIDDEN.value());
            body.put("error", "Forbidden");
            body.put("message", "Acesso negado. Você não possui permissão para gerenciar usuários.");
            body.put("errorCode", "AUTH_NO_MANAGE_PERMISSION");
            body.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
        }

        FuncionarioResponseDTO resultado = service.buscarAtivoPorMatricula(valor);

        if (resultado == null) { // verifica se não encontrou o funcionário
            Map<String, Object> body = new HashMap<>();
            body.put("status", HttpStatus.NOT_FOUND.value());
            body.put("error", "Not Found");
            body.put("message", "Nenhum funcionário encontrado para o valor: " + valor);
            body.put("errorCode", "AUTH_USER_NOT_FOUND");
            body.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
        }

        return ResponseEntity.ok(resultado);
    }


    @GetMapping("/buscar")
    public ResponseEntity<Object> buscar(@RequestParam String valor) {

        Object principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        String matriculaLogado;
        if (principal instanceof UserDetails userDetails) {
            matriculaLogado = userDetails.getUsername();
        } else {
            matriculaLogado = principal.toString();
        }

        List<String> permissoes = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        boolean temPermissao = permissoes.contains("GERENCIAR_PERMIS");

        if (!temPermissao) {
            Map<String, Object> body = new HashMap<>();
            body.put("status", HttpStatus.FORBIDDEN.value());
            body.put("error", "Forbidden");
            body.put("message", "Acesso negado. Você não possui permissão para gerenciar usuários.");
            body.put("errorCode", "AUTH_NO_MANAGE_PERMISSION");
            body.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
        }

        List<FuncionarioResponseDTO> resultados = service.buscarAtivosPorMatriculaOuNome(valor);

        if (resultados.isEmpty()) {
            Map<String, Object> body = new HashMap<>();
            body.put("status", HttpStatus.NOT_FOUND.value());
            body.put("error", "Not Found");
            body.put("message", "Nenhum funcionário encontrado para o valor: " + valor);
            body.put("errorCode", "AUTH_USER_NOT_FOUND");
            body.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
        }

        return ResponseEntity.ok(resultados);
    }

    @GetMapping("/todas-permissoes")
    public ResponseEntity<Object> listarTodos() {
        List<String> permissoes = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        boolean temPermissao = permissoes.contains("GERENCIAR_PERMIS");

        if (!temPermissao) {
            Map<String, Object> body = new HashMap<>();
            body.put("status", HttpStatus.FORBIDDEN.value());
            body.put("error", "Forbidden");
            body.put("message", "Acesso negado. Você não possui permissão para gerenciar usuários.");
            body.put("errorCode", "AUTH_NO_MANAGE_PERMISSION");
            body.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
        }

        return ResponseEntity.ok(service.listarTodosIndicadoresEPermissoes());
    }

    @PostMapping("/atualizar-permissoes")
    public ResponseEntity<Object> atualizarPermissoes(@RequestBody AtualizarPermissoesRequest request) {

        List<String> permissoes = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        boolean temPermissao = permissoes.contains("GERENCIAR_PERMIS");

        if (!temPermissao) {
            Map<String, Object> body = new HashMap<>();
            body.put("status", HttpStatus.FORBIDDEN.value());
            body.put("error", "Forbidden");
            body.put("message", "Acesso negado. Você não possui permissão para gerenciar usuários.");
            body.put("errorCode", "AUTH_NO_MANAGE_PERMISSION");
            body.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
        }

        service.atualizarPermissoes(request);

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "Permissões atualizadas com sucesso",
                "matricula", request.getMatricula()
        ));
    }
}
