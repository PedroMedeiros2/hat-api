package br.com.hat.hat_api.permissoes.controller;

import br.com.hat.hat_api.permissoes.dto.IndicadorPermissaoDTO;
import br.com.hat.hat_api.permissoes.model.Indicador;
import br.com.hat.hat_api.permissoes.repository.IndicadorRepository;
import br.com.hat.hat_api.permissoes.service.IndicadorPermissaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/permissoes")
public class IndicadorPermissaoController {

    @Autowired
    private IndicadorPermissaoService indicadorPermissaoService;
    @Autowired
    private IndicadorRepository indicadorRepository;

    @GetMapping
    public IndicadorPermissaoDTO getIndicadoresEPermissoes() {
        return indicadorPermissaoService.getIndicadoresEPermissoes();
    }

    @GetMapping("/buscar/{codigo}")
    public ResponseEntity<Object> buscarPorCodigo(@PathVariable String codigo) {
        Optional<Indicador> indicadorOpt = indicadorRepository.findByCodigo(codigo);

        if (indicadorOpt.isPresent()) {
            return ResponseEntity.ok(indicadorOpt.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Indicador com o código " + codigo + " não encontrado.");
        }
    }
}
