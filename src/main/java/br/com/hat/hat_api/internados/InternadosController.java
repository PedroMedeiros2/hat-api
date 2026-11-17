package br.com.hat.hat_api.internados;

import br.com.hat.hat_api.painel.BancoPainel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Pacientes internados (HAT0013, permissão indicadores:internados:ver). Dado pessoal sensível: cada
 * consulta fica registrada (quem, o quê, quando) antes de os dados saírem. A atualização automática da
 * tela (atualizacao=true) não gera linha nova: é a mesma consulta já registrada.
 */
@RestController
@RequestMapping("/api/internados")
public class InternadosController {

    private final InternadosService internados;
    private final RegistroConsultas registro;

    public InternadosController(InternadosService internados, RegistroConsultas registro) {
        this.internados = internados;
        this.registro = registro;
    }

    @GetMapping
    public Internados.Consulta consultar(@RequestParam(defaultValue = "false") boolean atualizacao, @AuthenticationPrincipal Jwt jwt) {
        if (!atualizacao) registro.registrar(usuario(jwt), new Internados.Registro("LISTA", null, null, null, null, null));
        else registro.preparar(); // sem o banco do registro, nem a atualização sai
        return internados.consultar();
    }

    /** A tela registra cada busca (depois de uma pausa na digitação) e cada ficha aberta. */
    @PostMapping("/consultas")
    public ResponseEntity<Void> registrar(@RequestBody Internados.Registro r, @AuthenticationPrincipal Jwt jwt) {
        registro.registrar(usuario(jwt), r);
        return ResponseEntity.noContent().build();
    }

    private static String usuario(Jwt jwt) {
        if (jwt == null) return "desconhecido";
        String u = jwt.getClaimAsString("usuario");
        return u != null && !u.isBlank() ? u.toLowerCase() : jwt.getSubject();
    }

    @ExceptionHandler(RegistroConsultas.DadoInvalido.class)
    ResponseEntity<Map<String, String>> invalido(RegistroConsultas.DadoInvalido e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage(), "errorCode", "INTERNADOS_DADO_INVALIDO"));
    }

    @ExceptionHandler(BancoPainel.BancoDesligado.class)
    ResponseEntity<Map<String, String>> desligado(BancoPainel.BancoDesligado e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "message", "O registro de consultas está fora do ar; por segurança (LGPD) a lista não é mostrada sem ele.",
                "errorCode", "INTERNADOS_SEM_REGISTRO"));
    }
}
