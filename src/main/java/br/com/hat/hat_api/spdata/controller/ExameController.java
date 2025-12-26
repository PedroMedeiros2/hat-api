package br.com.hat.hat_api.spdata.controller;

import br.com.hat.hat_api.spdata.dto.ExameDTO;
import br.com.hat.hat_api.spdata.service.ExameService;
import br.com.hat.hat_api.permissoes.service.UsuarioPermissaoService;
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
@RequestMapping("/api/exames")
@RequiredArgsConstructor
public class ExameController {

    private final ExameService exameService;
    private final UsuarioPermissaoService usuarioPermissaoService;

    private static final String INDICADOR_COD_CARDIO = "HAT0005";
    private static final String INDICADOR_COD_IMAGEM = "HAT0006";

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


    @GetMapping("/cardiologia")
    public ResponseEntity<?> getExamesCardio(
            @RequestParam String dataini,
            @RequestParam String datafim,
            Authentication authentication) {

        String matricula = authentication.getName();

        if (naoTemPermissaoVisualizar(authentication)) {
            return acessoNegado("Acesso negado. Você não possui a permissão VISUALIZAR.");
        }

        if (!usuarioPermissaoService.podeVisualizarIndicador(matricula, INDICADOR_COD_CARDIO)) {
            return acessoNegado("Acesso negado ao indicador de cardiologia.");
        }

        List<ExameDTO> exames = exameService.listarExamesCardio(dataini, datafim);
        return ResponseEntity.ok(exames);
    }


    @GetMapping("/imagem")
    public ResponseEntity<?> getExamesImagem(
            @RequestParam String dataini,
            @RequestParam String datafim,
            Authentication authentication) {

        String matricula = authentication.getName();

        if (naoTemPermissaoVisualizar(authentication)) {
            return acessoNegado("Acesso negado. Você não possui a permissão VISUALIZAR.");
        }

        if (!usuarioPermissaoService.podeVisualizarIndicador(matricula, INDICADOR_COD_IMAGEM)) {
            return acessoNegado("Acesso negado ao indicador de exames de imagem.");
        }

        List<ExameDTO> exames = exameService.listarExamesImagem(dataini, datafim);
        return ResponseEntity.ok(exames);
    }

}
