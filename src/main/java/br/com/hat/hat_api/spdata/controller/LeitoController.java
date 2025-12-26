package br.com.hat.hat_api.spdata.controller;

import br.com.hat.hat_api.spdata.dto.MovimentacaoDTO;
import br.com.hat.hat_api.spdata.dto.TaxaOcupacaoDTO;
import br.com.hat.hat_api.spdata.service.LeitoService;
import br.com.hat.hat_api.permissoes.service.UsuarioPermissaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/leitos")
@RequiredArgsConstructor
public class LeitoController {

    private final LeitoService leitoService;
    private final UsuarioPermissaoService usuarioPermissaoService;

    private static final String INDICADOR_COD_OCUPACAO_GERAL = "HAT0007";
    private static final String INDICADOR_COD_OCUPACAO_CNES = "HAT0008";
    private static final String INDICADOR_COD_MOVIMENTACAO = "HAT0002";

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

    @GetMapping("/ocupacao-geral")
    public ResponseEntity<?> getOcupacaoGeralPorBlocos(
            @RequestParam List<String> blocos,
            Authentication authentication
    ) {
        String matricula = authentication.getName();

        if (naoTemPermissaoVisualizar(authentication)) {
            return acessoNegado("Acesso negado. Você não possui a permissão VISUALIZAR.");
        }

        if (!usuarioPermissaoService.podeVisualizarIndicador(matricula, INDICADOR_COD_OCUPACAO_GERAL)) {
            return acessoNegado("Acesso negado ao indicador de ocupação geral de leitos.");
        }

        List<TaxaOcupacaoDTO> ocupacao = leitoService.getOcupacaoByBlocos(blocos);
        return ResponseEntity.ok(ocupacao);
    }

    @GetMapping("/ocupacao-cnes")
    public ResponseEntity<?> getOcupacaoCnesPorBlocos(
            @RequestParam List<String> blocos,
            Authentication authentication
    ) {
        String matricula = authentication.getName();

        if (naoTemPermissaoVisualizar(authentication)) {
            return acessoNegado("Acesso negado. Você não possui a permissão VISUALIZAR.");
        }

        if (!usuarioPermissaoService.podeVisualizarIndicador(matricula, INDICADOR_COD_OCUPACAO_CNES)) {
            return acessoNegado("Acesso negado ao indicador de ocupação de leitos - CNES.");
        }

        List<TaxaOcupacaoDTO> ocupacao = leitoService.getOcupacaoByBlocos(blocos);
        return ResponseEntity.ok(ocupacao);
    }

    @GetMapping("/movimentacao")
    public ResponseEntity<?> getMovimentacao(
            @RequestParam String dataini,
            @RequestParam String datafim,
            Authentication authentication
    ) {
        String matricula = authentication.getName();

        if (naoTemPermissaoVisualizar(authentication)) {
            return acessoNegado("Acesso negado. Você não possui a permissão VISUALIZAR.");
        }

        if (!usuarioPermissaoService.podeVisualizarIndicador(matricula, INDICADOR_COD_MOVIMENTACAO)) {
            return acessoNegado("Acesso negado ao indicador de movimentação de leitos.");
        }

        List<MovimentacaoDTO> movimentacao = leitoService.getMovimentacao(dataini, datafim);
        return ResponseEntity.ok(movimentacao);
    }
}
