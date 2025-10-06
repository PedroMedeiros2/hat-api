package br.com.hat.hat_api.controller;

import br.com.hat.hat_api.dto.MovimentacaoDTO;
import br.com.hat.hat_api.dto.TaxaOcupacaoDTO;
import br.com.hat.hat_api.service.LeitoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.web.bind.annotation.*;

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
