package br.com.hat.hat_api.painel.cnes;

import br.com.hat.hat_api.painel.BancoPainel;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CnesServiceTest {

    @Test
    void semBancoLeOCadastroInicialENaoEdita() {
        CnesService s = new CnesService(new BancoPainel("", "", ""));
        assertThat(s.editavel()).isFalse();
        assertThat(s.listar()).hasSize(230);
        assertThatThrownBy(() -> s.criar(new LeitoCnes.Dados("AP100", "Q:105", "L:1", "SUS", "CLÍNICA GERAL", null, null, true, null), "teste"))
                .isInstanceOf(BancoPainel.BancoDesligado.class);
    }

    @Test
    void importaUmaVezEditaEGuardaOHistorico() {
        CnesService s = new CnesService(BancoPainel.emMemoria("cnes1"));
        assertThat(s.listar()).hasSize(230);
        assertThat(s.listar()).hasSize(230);

        LeitoCnes novo = s.criar(new LeitoCnes.Dados("ap100", "Q:105", "L:1", "SUS", "CLÍNICA GERAL", null, null, true, null), "maria");
        assertThat(novo.bloco()).isEqualTo("AP100");
        assertThat(novo.ordem()).isEqualTo(17);

        LeitoCnes mudado = s.salvar(novo.id(), new LeitoCnes.Dados("AP100", "Q:105", "L:1", "CONV/PART", "CLÍNICA GERAL", null, null, false, null), "joao");
        assertThat(mudado.ativo()).isFalse();
        assertThat(mudado.atualizadoPor()).isEqualTo("joao");

        var h = s.historico(10);
        assertThat(h).extracting(LeitoCnes.Mudanca::acao).containsExactly("DESATIVAR", "CRIAR", "IMPORTAR");
        assertThat(h.get(0).antes()).contains("SUS").doesNotContain("inativo");
        assertThat(h.get(0).depois()).contains("CONV/PART").contains("inativo");
    }

    @Test
    void tiposDeLeitoEDoBloco() {
        CnesService s = new CnesService(BancoPainel.emMemoria("cnes3"));
        var c = s.cadastro();
        assertThat(c.tipos()).extracting(LeitoCnes.TipoLeito::codigo).containsExactly("FIXO", "PROVISORIO", "VIRTUAL");
        assertThat(c.blocos()).extracting(LeitoCnes.BlocoTipo::bloco).containsExactlyInAnyOrder("PS", "PA");

        s.salvarTipo("hemodialise", new LeitoCnes.DadosTipo("Hemodiálise", false, true, null, null), "maria");
        s.definirTipoBloco("uti", "HEMODIALISE", "maria");
        assertThat(s.blocos()).contains(new LeitoCnes.BlocoTipo("UTI", "HEMODIALISE"));
        s.definirTipoBloco("UTI", "", "maria");
        assertThat(s.blocos()).extracting(LeitoCnes.BlocoTipo::bloco).doesNotContain("UTI");

        LeitoCnes l = s.criar(new LeitoCnes.Dados("AP100", "Q:199", "L:1", "SUS", "CLÍNICA GERAL", null, null, true, "virtual"), "maria");
        assertThat(l.tipo()).isEqualTo("VIRTUAL");
        assertThatThrownBy(() -> s.definirTipoBloco("AP100", "NAOEXISTE", "x")).isInstanceOf(CnesService.DadoInvalido.class);
        assertThatThrownBy(() -> s.salvarTipo("X1", new LeitoCnes.DadosTipo("Dois", true, true, null, null), "x")).isInstanceOf(CnesService.DadoInvalido.class);
    }

    @Test
    void recusaPerfilInvalidoELeitoRepetido() {
        CnesService s = new CnesService(BancoPainel.emMemoria("cnes2"));
        assertThatThrownBy(() -> s.criar(new LeitoCnes.Dados("UTI", "BOX", "11", "PARTICULAR", "CTI", null, null, true, null), "x"))
                .isInstanceOf(CnesService.DadoInvalido.class);
        assertThatThrownBy(() -> s.criar(new LeitoCnes.Dados("AP100", "Q:100", "L:1", "CONV/PART", "SAÚDE MENTAL", null, null, true, null), "x"))
                .isInstanceOf(CnesService.DadoInvalido.class).hasMessageContaining("igual");
    }
}
