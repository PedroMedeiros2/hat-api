package br.com.hat.hat_api.spdata.controller;

import br.com.hat.hat_api.spdata.dto.InternacaoDetalhadaDTO;
import br.com.hat.hat_api.spdata.dto.MovimentacaoDTO;
import br.com.hat.hat_api.spdata.dto.SaidaDTO;
import br.com.hat.hat_api.spdata.dto.TaxaOcupacaoDTO;
import br.com.hat.hat_api.spdata.service.BlocoService;
import br.com.hat.hat_api.spdata.service.LeitoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leitos")
@RequiredArgsConstructor
public class LeitoController {
    private final LeitoService leitoService;
    private final BlocoService blocoService;

    @GetMapping("/ocupacao-geral")
    public ResponseEntity<?> getOcupacaoGeralPorBlocos(
            @RequestParam(required = false) List<String> blocos) {
        List<TaxaOcupacaoDTO> ocupacao = leitoService.getOcupacaoByBlocos(blocoService.resolverBlocos(blocos));
        return ResponseEntity.ok(ocupacao);
    }

    @GetMapping("/ocupacao-cnes")
    public ResponseEntity<?> getOcupacaoCnesPorBlocos(
            @RequestParam(required = false) List<String> blocos) {
        List<TaxaOcupacaoDTO> ocupacao = leitoService.getOcupacaoByBlocos(blocoService.resolverBlocos(blocos));
        return ResponseEntity.ok(ocupacao);
    }

    @GetMapping("/movimentacao")
    public ResponseEntity<?> getMovimentacao(
            @RequestParam String dataini,
            @RequestParam String datafim) {
        List<MovimentacaoDTO> movimentacao = leitoService.getMovimentacao(dataini, datafim);
        return ResponseEntity.ok(movimentacao);
    }

    /** Ficha de uma internação pelo registro (HAT0002): CID, procedimento, leito e alta prevista. */
    @GetMapping("/ficha")
    public ResponseEntity<?> getFicha(@RequestParam Long reg) {
        return leitoService.getFicha(reg)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** Altas e óbitos caso a caso (HAT0002). */
    @GetMapping("/saidas")
    public ResponseEntity<?> getSaidas(
            @RequestParam String dataini,
            @RequestParam String datafim) {
        List<SaidaDTO> saidas = leitoService.getSaidas(dataini, datafim);
        return ResponseEntity.ok(saidas);
    }

    @GetMapping("/internacoes")
    public ResponseEntity<?> getInternacoesDetalhadas(
            @RequestParam String dataini,
            @RequestParam String datafim) {
        List<InternacaoDetalhadaDTO> internacoes = leitoService.getInternacoesDetalhadas(dataini, datafim);
        return ResponseEntity.ok(internacoes);
    }
}
