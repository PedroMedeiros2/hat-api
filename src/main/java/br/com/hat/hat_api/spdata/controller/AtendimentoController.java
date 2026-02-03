package br.com.hat.hat_api.spdata.controller;

import br.com.hat.hat_api.permissoes.service.UsuarioPermissaoService;
import br.com.hat.hat_api.spdata.dto.AtendimentoDTO;
import br.com.hat.hat_api.spdata.service.AtendimentoService;
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
@RequestMapping("/api/atendimentos")
@RequiredArgsConstructor
public class AtendimentoController {

    private final AtendimentoService atendimentoService;
    private final UsuarioPermissaoService usuarioPermissaoService;

    private static final String INDICADOR_COD_ATENDIMENTO = "HAT0007"; // ajuste se necessário

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
        List<String> permissoes = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return !permissoes.contains("VISUALIZAR");
    }

    @GetMapping
    public ResponseEntity<?> getAtendimentos(
            @RequestParam String dataini,
            @RequestParam String datafim,
            Authentication authentication) {

        String matricula = authentication.getName();

        if (naoTemPermissaoVisualizar(authentication)) {
            return acessoNegado("Acesso negado. Você não possui a permissão VISUALIZAR.");
        }

        if (!usuarioPermissaoService.podeVisualizarIndicador(matricula, INDICADOR_COD_ATENDIMENTO)) {
            return acessoNegado("Acesso negado ao indicador de atendimentos.");
        }

        List<AtendimentoDTO> atendimentos =
                atendimentoService.listarAtendimentos(dataini, datafim);

        return ResponseEntity.ok(atendimentos);
    }
}
