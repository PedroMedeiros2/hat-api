package br.com.hat.hat_api.painel.ocupacao;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/** Histórico da ocupação (fotos diárias do banco do painel): HAT0007 geral e HAT0008 CNES (seguranca/Permissoes). */
@RestController
@RequestMapping("/api/leitos")
public class OcupacaoHistoricoController {

    private final OcupacaoDiaria diaria;
    private final FotoOcupacao foto;

    public OcupacaoHistoricoController(OcupacaoDiaria diaria, FotoOcupacao foto) {
        this.diaria = diaria;
        this.foto = foto;
    }

    /** hora = horário da foto (HH:mm); dias = do mais antigo ao mais novo, até ontem. */
    public record Resposta(boolean disponivel, String hora, LocalDate desde, List<OcupacaoDiaria.Dia> dias) { }

    @GetMapping("/ocupacao-geral/historico")
    public Resposta geral(@RequestParam(defaultValue = "7") int dias) {
        return ler(ContaOcupacao.GERAL, dias);
    }

    @GetMapping("/ocupacao-cnes/historico")
    public Resposta cnes(@RequestParam(defaultValue = "7") int dias) {
        return ler(ContaOcupacao.CNES, dias);
    }

    private Resposta ler(String base, int dias) {
        var h = diaria.ler(base, dias, FotoOcupacao.hoje());
        return new Resposta(h.disponivel(), foto.hora().toString(), h.desde(), h.dias());
    }
}
