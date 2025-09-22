package br.com.hat.hat_api.controller;

import br.com.hat.hat_api.dto.MovimentacaoDTO;
import br.com.hat.hat_api.dto.TaxaOcupacaoDTO;
import br.com.hat.hat_api.service.LeitoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leitos")
public class LeitoController {

    private final LeitoService leitoService;

    public LeitoController(LeitoService leitoService) {
        this.leitoService = leitoService;
    }

    @GetMapping("/ocupacao")
    public List<TaxaOcupacaoDTO> getOcupacaoPorBlocos(
            @RequestParam List<String> blocos
    ) {
        return leitoService.getOcupacaoByBlocos(blocos);
    }

    @GetMapping("/movimentacao")
    public List<MovimentacaoDTO> getMovimentacao(
            @RequestParam String dataini,
            @RequestParam String datafim
    ) {
        return leitoService.getMovimentacao(dataini, datafim);
    }
}
