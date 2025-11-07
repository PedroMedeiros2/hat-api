package br.com.hat.hat_api.spdata.controller;

import br.com.hat.hat_api.spdata.dto.MovimentacaoDTO;
import br.com.hat.hat_api.spdata.dto.TaxaOcupacaoDTO;
import br.com.hat.hat_api.spdata.service.LeitoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leitos")
@RequiredArgsConstructor
public class LeitoController {

    private final LeitoService leitoService;

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
