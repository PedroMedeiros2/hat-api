package br.com.hat.hat_api.painel.cnes;

import br.com.hat.hat_api.painel.BancoPainel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Cadastro CNES: leitura para as telas de ocupação (sem dado de paciente, basta ter entrado) e edição com
 * a permissão indicadores:cnes:editar (seguranca/Permissoes).
 */
@RestController
@RequestMapping("/api/cnes")
public class CnesController {

    private final CnesService cnes;

    public CnesController(CnesService cnes) {
        this.cnes = cnes;
    }

    @GetMapping("/leitos")
    public LeitoCnes.Cadastro listar() {
        return cnes.cadastro();
    }

    /** Cria ou muda um tipo de leito. */
    @PutMapping("/tipos/{codigo}")
    public LeitoCnes.TipoLeito salvarTipo(@PathVariable String codigo, @RequestBody LeitoCnes.DadosTipo dados, @AuthenticationPrincipal Jwt jwt) {
        return cnes.salvarTipo(codigo, dados, usuario(jwt));
    }

    /** Define o tipo padrão de um bloco ({"tipo": "PROVISORIO"}; vazio = Fixo). */
    @PutMapping("/blocos/{bloco}")
    public LeitoCnes.BlocoTipo definirTipoBloco(@PathVariable String bloco, @RequestBody Map<String, String> corpo, @AuthenticationPrincipal Jwt jwt) {
        return cnes.definirTipoBloco(bloco, corpo == null ? null : corpo.get("tipo"), usuario(jwt));
    }

    @PostMapping("/leitos")
    public ResponseEntity<LeitoCnes> criar(@RequestBody LeitoCnes.Dados dados, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cnes.criar(dados, usuario(jwt)));
    }

    @PutMapping("/leitos/{id}")
    public LeitoCnes salvar(@PathVariable long id, @RequestBody LeitoCnes.Dados dados, @AuthenticationPrincipal Jwt jwt) {
        return cnes.salvar(id, dados, usuario(jwt));
    }

    @GetMapping("/historico")
    public List<LeitoCnes.Mudanca> historico(@RequestParam(defaultValue = "500") int limite) {
        return cnes.historico(limite);
    }

    private static String usuario(Jwt jwt) {
        if (jwt == null) return "desconhecido";
        String u = jwt.getClaimAsString("usuario");
        return u != null && !u.isBlank() ? u.toLowerCase() : jwt.getSubject();
    }

    @ExceptionHandler(CnesService.DadoInvalido.class)
    ResponseEntity<Map<String, String>> invalido(CnesService.DadoInvalido e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage(), "errorCode", "CNES_DADO_INVALIDO"));
    }

    @ExceptionHandler(CnesService.NaoEncontrado.class)
    ResponseEntity<Map<String, String>> naoEncontrado(CnesService.NaoEncontrado e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage(), "errorCode", "CNES_NAO_ENCONTRADO"));
    }

    @ExceptionHandler(BancoPainel.BancoDesligado.class)
    ResponseEntity<Map<String, String>> desligado(BancoPainel.BancoDesligado e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("message", e.getMessage(), "errorCode", "CNES_SEM_BANCO"));
    }
}
