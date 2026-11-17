package br.com.hat.hat_api.internados;

import br.com.hat.hat_api.painel.BancoPainel;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InternadosTest {

    private static Object[] leito(Long reg, String bloco, String nome, String mae, String pai) {
        return new Object[]{reg, reg == null ? null : 10L, reg == null ? null : Date.valueOf("2026-11-10"), reg == null ? null : "1430",
                bloco + "   ", "100 ", "001", nome, reg == null ? null : Date.valueOf("1950-03-02"), "F", mae, pai, "MEDICO A", "SUS", "CLINICA"};
    }

    @Test
    void contaLeitosEInternadosPorBlocoEOrdenaPorNome() {
        var c = InternadosService.montar(List.of(
                leito(2L, "AP100", "MARIA SILVA ", "ANA SILVA", "   "),
                leito(null, "AP100", null, null, null),
                leito(1L, "UTI", "ÁUREA COSTA", "JOANA COSTA", "JOSE COSTA")), LocalDateTime.of(2026, 11, 17, 8, 0));

        assertThat(c.blocos()).containsExactly(new Internados.Bloco("AP100", 1, 2), new Internados.Bloco("UTI", 1, 1));
        assertThat(c.pacientes()).extracting(Internados.Paciente::nome).containsExactly("ÁUREA COSTA", "MARIA SILVA");
        var maria = c.pacientes().get(1);
        assertThat(maria.pai()).isNull();
        assertThat(maria.mae()).isEqualTo("ANA SILVA");
        assertThat(maria.entrada()).isEqualTo(LocalDateTime.of(2026, 11, 10, 14, 30));
        assertThat(maria.nascimento()).isEqualTo(LocalDate.of(1950, 3, 2));
    }

    @Test
    void horaInvalidaViraMeiaNoite() {
        assertThat(InternadosService.dataHora(Date.valueOf("2026-11-10"), "9999")).isEqualTo(LocalDateTime.of(2026, 11, 10, 0, 0));
        assertThat(InternadosService.dataHora(null, "1200")).isNull();
    }

    @Test
    void registraQuemConsultouOQueEQuando() {
        var r = new RegistroConsultas(BancoPainel.emMemoria("internados1"));
        r.registrar("maria", new Internados.Registro("lista", null, null, null, null, null));
        r.registrar("maria", new Internados.Registro("BUSCA", "  jose " + "x".repeat(300), "filiacao", "UTI", null, 3));
        r.registrar("joao", new Internados.Registro("FICHA", null, null, null, 123L, null));

        var u = r.ultimas(10);
        assertThat(u).extracting(Internados.Linha::acao).containsExactly("FICHA", "BUSCA", "LISTA");
        assertThat(u.get(0).usuario()).isEqualTo("joao");
        assertThat(u.get(0).reg()).isEqualTo(123L);
        assertThat(u.get(1).busca()).startsWith("jose x").hasSize(RegistroConsultas.MAX_BUSCA);
        assertThat(u.get(1).resultados()).isEqualTo(3);
        assertThat(u.get(2).quando()).isNotNull();
    }

    @Test
    void acaoDesconhecidaOuSemBancoNaoRegistra() {
        var r = new RegistroConsultas(BancoPainel.emMemoria("internados2"));
        assertThatThrownBy(() -> r.registrar("maria", new Internados.Registro("EXPORTAR", null, null, null, null, null)))
                .isInstanceOf(RegistroConsultas.DadoInvalido.class);
        var sem = new RegistroConsultas(new BancoPainel("", "", ""));
        assertThatThrownBy(() -> sem.registrar("maria", new Internados.Registro("LISTA", null, null, null, null, null)))
                .isInstanceOf(BancoPainel.BancoDesligado.class);
    }
}
