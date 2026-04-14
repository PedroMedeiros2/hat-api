package br.com.hat.hat_api.spdata.controller;

import br.com.hat.hat_api.permissoes.service.UsuarioPermissaoService;
import br.com.hat.hat_api.spdata.dto.AgendaCirurgicaDTO;
import br.com.hat.hat_api.spdata.dto.CancelamentoCirurgicoDTO;
import br.com.hat.hat_api.spdata.dto.ExameHemodinamicaDTO;
import br.com.hat.hat_api.spdata.dto.HemodinamicaDTO;
import br.com.hat.hat_api.spdata.service.HemodinamicaService;
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

@RestController
@RequestMapping("/api/hemodinamica")
@RequiredArgsConstructor
public class HemodinamicaController {

    private final HemodinamicaService hemodinamicaService;
    private final UsuarioPermissaoService usuarioPermissaoService;

    private static final String INDICADOR_COD_HEMODINAMICA = "HAT0015";

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

    @GetMapping
    public ResponseEntity<?> getHemodinamica(
            @RequestParam String dataini,
            @RequestParam String datafim,
            Authentication authentication) {

        String matricula = authentication.getName();

        if (naoTemPermissaoVisualizar(authentication)) {
            return acessoNegado("Acesso negado. Você não possui a permissão VISUALIZAR.");
        }

        if (!usuarioPermissaoService.podeVisualizarIndicador(matricula, INDICADOR_COD_HEMODINAMICA)) {
            return acessoNegado("Acesso negado ao indicador de hemodinâmica.");
        }

        List<HemodinamicaDTO> resultado = hemodinamicaService.listarHemodinamica(dataini, datafim);

        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/exames")
    public ResponseEntity<?> getExamesHemodinamica(
            @RequestParam String dataini,
            @RequestParam String datafim,
            Authentication authentication) {

        String matricula = authentication.getName();

        if (naoTemPermissaoVisualizar(authentication)) {
            return acessoNegado("Acesso negado. Você não possui a permissão VISUALIZAR.");
        }

        if (!usuarioPermissaoService.podeVisualizarIndicador(matricula, INDICADOR_COD_HEMODINAMICA)) {
            return acessoNegado("Acesso negado ao indicador de hemodinâmica.");
        }

        List<ExameHemodinamicaDTO> resultado = hemodinamicaService.listarExamesHemodinamica(dataini, datafim);

        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/cancelamentos")
    public ResponseEntity<?> getCancelamentosHemodinamica(
            @RequestParam String dataini,
            @RequestParam String datafim,
            Authentication authentication) {

        String matricula = authentication.getName();

        if (naoTemPermissaoVisualizar(authentication)) {
            return acessoNegado("Acesso negado. Você não possui a permissão VISUALIZAR.");
        }

        if (!usuarioPermissaoService.podeVisualizarIndicador(matricula, INDICADOR_COD_HEMODINAMICA)) {
            return acessoNegado("Acesso negado ao indicador de hemodinâmica.");
        }

        List<CancelamentoCirurgicoDTO> resultado = hemodinamicaService.listarCancelamentos(dataini, datafim);

        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/agenda")
    public ResponseEntity<?> getAgendaHemodinamica(
            @RequestParam String dataini,
            @RequestParam String datafim,
            Authentication authentication) {

        String matricula = authentication.getName();

        if (naoTemPermissaoVisualizar(authentication)) {
            return acessoNegado("Acesso negado. Você não possui a permissão VISUALIZAR.");
        }

        if (!usuarioPermissaoService.podeVisualizarIndicador(matricula, INDICADOR_COD_HEMODINAMICA)) {
            return acessoNegado("Acesso negado ao indicador de hemodinâmica.");
        }

        List<AgendaCirurgicaDTO> resultado = hemodinamicaService.listarAgenda(dataini, datafim);

        return ResponseEntity.ok(resultado);
    }
}