package br.com.hat.hat_api.painel.ocupacao;

import br.com.hat.hat_api.painel.BancoPainel;
import br.com.hat.hat_api.painel.cnes.CnesService;
import br.com.hat.hat_api.painel.cnes.LeitoCnes;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OcupacaoDiariaTest {

    private static ContaOcupacao.Linha linha(String base, String bloco, int ativos, int ocupados) {
        return new ContaOcupacao.Linha(bloco, bloco, "FIXO", "Fixo", false, base, ativos, ocupados, ocupados, 0, 0);
    }

    @Test
    void gravaSubstituiNoMesmoDiaELeNDias() {
        OcupacaoDiaria d = new OcupacaoDiaria(BancoPainel.emMemoria("ocup1"));
        LocalDate hoje = LocalDate.of(2026, 11, 17);
        for (int i = 1; i <= 10; i++) {
            LocalDate dia = hoje.minusDays(i);
            d.gravar(dia, dia.plusDays(1).atTime(0, 5), OcupacaoDiaria.AGENDADA,
                    List.of(linha("geral", "AP100", 20, 10 + i), linha("cnes", "AP100", 18, 10 + i)));
        }
        // de novo no mesmo dia: troca a foto, não soma
        d.gravar(hoje.minusDays(1), LocalDateTime.of(2026, 11, 17, 9, 0), OcupacaoDiaria.ATRASADA,
                List.of(linha("geral", "AP100", 20, 15), linha("geral", "UTI", 10, 9)));

        var h = d.ler("geral", 7, hoje);
        assertThat(h.disponivel()).isTrue();
        assertThat(h.desde()).isEqualTo(hoje.minusDays(10));
        assertThat(h.dias()).hasSize(7);
        assertThat(h.dias().get(0).dia()).isEqualTo(hoje.minusDays(7));
        var ontem = h.dias().get(6);
        assertThat(ontem.origem()).isEqualTo(OcupacaoDiaria.ATRASADA);
        assertThat(ontem.momento()).isEqualTo(LocalDateTime.of(2026, 11, 17, 9, 0));
        assertThat(ontem.blocos()).extracting(OcupacaoDiaria.Bloco::ocupados).containsExactly(15, 9);
        // a troca apagou a base cnes daquele dia também (uma foto por dia)
        assertThat(d.ler("cnes", 1, hoje).dias()).isEmpty();
        assertThat(d.diasGravados(hoje.minusDays(3), hoje)).hasSize(3);
    }

    @Test
    void semBancoNaoTemHistorico() {
        OcupacaoDiaria d = new OcupacaoDiaria(new BancoPainel("", "", ""));
        assertThat(d.configurado()).isFalse();
        var h = d.ler("geral", 7, LocalDate.now());
        assertThat(h.disponivel()).isFalse();
        assertThat(h.dias()).isEmpty();
    }

    @Test
    void diaEHorarioDaFoto() {
        LocalTime t = LocalTime.of(0, 5);
        assertThat(FotoOcupacao.diaDaFoto(LocalDateTime.of(2026, 11, 18, 0, 5), t)).isEqualTo(LocalDate.of(2026, 11, 17));
        assertThat(FotoOcupacao.momentoDoDia(LocalDate.of(2026, 11, 17), t)).isEqualTo(LocalDateTime.of(2026, 11, 18, 0, 5));
        assertThat(FotoOcupacao.ultimoDiaFechado(LocalDateTime.of(2026, 11, 18, 0, 4), t)).isEqualTo(LocalDate.of(2026, 11, 16));
        assertThat(FotoOcupacao.ultimoDiaFechado(LocalDateTime.of(2026, 11, 18, 0, 5), t)).isEqualTo(LocalDate.of(2026, 11, 17));
        LocalTime noite = LocalTime.of(23, 50);
        assertThat(FotoOcupacao.ultimoDiaFechado(LocalDateTime.of(2026, 11, 18, 12, 0), noite)).isEqualTo(LocalDate.of(2026, 11, 17));
    }

    @Test
    void contaComoATelaETiposAParte() {
        var cad = new CnesService(new BancoPainel("", "", "")).cadastro(); // tipos iniciais: PS provisório
        var cnes = List.of(
                new LeitoCnes(1L, "AP100", "Q:100", "L:1", "SUS", "-", null, 1, true, null, null, null),
                new LeitoCnes(2L, "AP100", "Q:101", "L:1", "SUS", "-", null, 2, true, null, null, null));
        cad = new LeitoCnes.Cadastro(false, cnes, cad.tipos(), cad.blocos(), cad.tipoPadrao());
        var sp = List.of(
                new ContaOcupacao.LeitoSp("AP100", "100", "001", "SUS"),
                new ContaOcupacao.LeitoSp("AP100", "102", "001", "Particular"), // fora do cadastro CNES
                new ContaOcupacao.LeitoSp("PS", "PS", "01", "Convenio"),
                new ContaOcupacao.LeitoSp("PS", "PS", "02", null));
        var l = ContaOcupacao.contar(sp, cad);
        var geralAp = l.stream().filter(x -> x.base().equals("geral") && x.bloco().equals("AP100")).findFirst().orElseThrow();
        var cnesAp = l.stream().filter(x -> x.base().equals("cnes") && x.bloco().equals("AP100")).findFirst().orElseThrow();
        var ps = l.stream().filter(x -> x.base().equals("geral") && x.bloco().equals("PS")).findFirst().orElseThrow();
        assertThat(geralAp.ativos()).isEqualTo(2);
        assertThat(cnesAp.ativos()).isEqualTo(2); // 100 casou; 101 só no CNES
        assertThat(geralAp.ocupados()).isEqualTo(2);
        assertThat(geralAp.sus()).isEqualTo(1);
        assertThat(geralAp.particular()).isEqualTo(1);
        assertThat(ps.aParte()).isTrue();
        assertThat(ps.grupo()).isEqualTo("PS|PROVISORIO");
        assertThat(ps.convenio()).isEqualTo(1);
    }

    @Test
    void reconstroiQuemEstavaNoLeito() {
        var base = List.of(new ContaOcupacao.LeitoSp("AP100", "100", "001", null), new ContaOcupacao.LeitoSp("UTI", "BOX", "01", null));
        LocalDateTime m = LocalDateTime.of(2026, 11, 11, 0, 5);
        var ps = List.of(
                new FotoOcupacao.Passagem("AP100", "100", "001", m.minusDays(2), m.plusHours(3), "SUS"),
                new FotoOcupacao.Passagem("AP100", "105", "001", m.minusDays(1), null, "Particular"), // leito hoje inativo
                new FotoOcupacao.Passagem("UTI", "BOX", "01", m.minusDays(3), m.minusHours(1), "SUS"), // já tinha saído
                new FotoOcupacao.Passagem("FECHADO", "1", "1", m.minusDays(1), null, "SUS")); // bloco sem leito ativo hoje
        var l = FotoOcupacao.leitosNoMomento(base, ps, m);
        assertThat(l).extracting(ContaOcupacao.LeitoSp::quarto).containsExactly("100", "105", "BOX");
        assertThat(l).extracting(ContaOcupacao.LeitoSp::tipoConvenio).containsExactly("SUS", "Particular", null);
    }
}
