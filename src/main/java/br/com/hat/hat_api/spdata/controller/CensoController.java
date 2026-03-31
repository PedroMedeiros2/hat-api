package br.com.hat.hat_api.spdata.controller;

import br.com.hat.hat_api.permissoes.service.UsuarioPermissaoService;
import br.com.hat.hat_api.spdata.dto.BlocoDTO;
import br.com.hat.hat_api.spdata.dto.CensoDTO;
import br.com.hat.hat_api.spdata.service.CensoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/censo")
@RequiredArgsConstructor
public class CensoController {

    private final CensoService censoService;
    private final UsuarioPermissaoService usuarioPermissaoService;

    private static final String INDICADOR_COD_CENSO = "HAT0010";

    private ResponseEntity<Map<String, Object>> acessoNegado(String mensagem) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("error", "Forbidden");
        body.put("message", mensagem);
        body.put("errorCode", "INDICADOR_ACCESS_DENIED");
        body.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    private boolean naoTemPermissaoVisualizar(Authentication authentication) {
        return authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .noneMatch("VISUALIZAR"::equals);
    }

    @GetMapping("/todos-blocos")
    public ResponseEntity<?> getCensoTodosBlocos(
            @RequestParam String dataini,
            @RequestParam String datafim,
            @RequestParam(required = false, defaultValue = "1") Integer incluirMesmoDia,
            Authentication authentication) {

        String matricula = authentication.getName();

        if (naoTemPermissaoVisualizar(authentication)) {
            return acessoNegado("Acesso negado. Você não possui a permissão VISUALIZAR.");
        }

        if (!usuarioPermissaoService.podeVisualizarIndicador(matricula, INDICADOR_COD_CENSO)) {
            return acessoNegado("Acesso negado ao indicador de censo.");
        }

        List<String> blocos = List.of(
                "AP100","AP200","AP300","AP400","AP500","AP600",
                "UTI","PA","PS"
        );

        List<CensoDTO> censo = censoService.listarCensoMultiplosBlocos(
                dataini,
                datafim,
                blocos,
                incluirMesmoDia
        );

        return ResponseEntity.ok(censo);
    }

    @GetMapping
    public ResponseEntity<?> getCenso(
            @RequestParam String dataini,
            @RequestParam String datafim,
            @RequestParam String bloco,
            @RequestParam(required = false, defaultValue = "1") Integer incluirMesmoDia,
            Authentication authentication) {

        String matricula = authentication.getName();

        if (naoTemPermissaoVisualizar(authentication)) {
            return acessoNegado("Acesso negado.");
        }

        if (!usuarioPermissaoService.podeVisualizarIndicador(matricula, INDICADOR_COD_CENSO)) {
            return acessoNegado("Acesso negado ao indicador.");
        }

        List<CensoDTO> censo = censoService.listarCenso(
                dataini,
                datafim,
                bloco,
                incluirMesmoDia
        );

        return ResponseEntity.ok(censo);
    }

    @GetMapping("/blocos")
    public ResponseEntity<?> getBlocos(Authentication authentication) {

        String matricula = authentication.getName();

        if (naoTemPermissaoVisualizar(authentication)) {
            return acessoNegado("Acesso negado.");
        }

        if (!usuarioPermissaoService.podeVisualizarIndicador(matricula, INDICADOR_COD_CENSO)) {
            return acessoNegado("Acesso negado ao indicador.");
        }

        List<BlocoDTO> blocos = censoService.listarBlocos();

        return ResponseEntity.ok(blocos);
    }
}
