package br.com.hat.hat_api.painel.ocupacao;

import br.com.hat.hat_api.painel.BancoPainel;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Tabela OCUPACAO_DIARIA do banco do painel: uma foto por dia (o censo fechado do dia), por bloco e por base
 * (geral e cnes). Só contagens, nenhum dado de paciente. Gravar de novo o mesmo dia substitui a foto.
 */
@Service
public class OcupacaoDiaria {

    /** Origem da foto: na hora marcada, depois da hora (a API estava fora) ou reconstruída pela RICTRLOC. */
    public static final String AGENDADA = "agendada", ATRASADA = "atrasada", RECONSTRUIDA = "reconstruida";
    public static final int DIAS_MAX = 90;

    private final BancoPainel banco;
    private volatile boolean pronto;

    public OcupacaoDiaria(BancoPainel banco) {
        this.banco = banco;
    }

    public boolean configurado() {
        return banco.configurado();
    }

    synchronized void preparar() {
        if (pronto) return;
        banco.jdbc().execute("""
                CREATE TABLE IF NOT EXISTS OCUPACAO_DIARIA (
                    DIA DATE NOT NULL,
                    MOMENTO TIMESTAMP NOT NULL,
                    ORIGEM VARCHAR(12) NOT NULL,
                    BASE VARCHAR(5) NOT NULL,
                    GRUPO VARCHAR(50) NOT NULL,
                    BLOCO VARCHAR(20) NOT NULL,
                    TIPO VARCHAR(20) NOT NULL,
                    TIPO_NOME VARCHAR(40) NOT NULL,
                    A_PARTE BOOLEAN NOT NULL,
                    ATIVOS INT NOT NULL,
                    OCUPADOS INT NOT NULL,
                    SUS INT NOT NULL,
                    PARTICULAR INT NOT NULL,
                    CONVENIO INT NOT NULL,
                    PRIMARY KEY (DIA, BASE, GRUPO)
                )""");
        pronto = true;
    }

    /** Grava a foto do dia, trocando a que já existia (tudo ou nada). */
    public void gravar(LocalDate dia, LocalDateTime momento, String origem, List<ContaOcupacao.Linha> linhas) {
        preparar();
        banco.jdbc().execute((Connection con) -> {
            boolean auto = con.getAutoCommit();
            con.setAutoCommit(false);
            try (PreparedStatement del = con.prepareStatement("DELETE FROM OCUPACAO_DIARIA WHERE DIA = ?");
                 PreparedStatement ins = con.prepareStatement("INSERT INTO OCUPACAO_DIARIA (DIA, MOMENTO, ORIGEM, BASE, GRUPO, BLOCO, TIPO, TIPO_NOME, A_PARTE, ATIVOS, OCUPADOS, SUS, PARTICULAR, CONVENIO) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)")) {
                del.setDate(1, Date.valueOf(dia));
                del.executeUpdate();
                for (ContaOcupacao.Linha l : linhas) {
                    ins.setDate(1, Date.valueOf(dia));
                    ins.setTimestamp(2, Timestamp.valueOf(momento));
                    ins.setString(3, origem);
                    ins.setString(4, l.base());
                    ins.setString(5, l.grupo());
                    ins.setString(6, l.bloco());
                    ins.setString(7, l.tipo());
                    ins.setString(8, l.tipoNome());
                    ins.setBoolean(9, l.aParte());
                    ins.setInt(10, l.ativos());
                    ins.setInt(11, l.ocupados());
                    ins.setInt(12, l.sus());
                    ins.setInt(13, l.particular());
                    ins.setInt(14, l.convenio());
                    ins.addBatch();
                }
                ins.executeBatch();
                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(auto);
            }
            return null;
        });
    }

    /** Dias que já têm foto, entre os dois (inclusive). */
    public Set<LocalDate> diasGravados(LocalDate de, LocalDate ate) {
        preparar();
        return new HashSet<>(banco.jdbc().query("SELECT DISTINCT DIA FROM OCUPACAO_DIARIA WHERE DIA BETWEEN ? AND ?",
                (r, i) -> r.getDate(1).toLocalDate(), Date.valueOf(de), Date.valueOf(ate)));
    }

    public record Bloco(String grupo, String bloco, String tipoNome, boolean aParte, int ativos, int ocupados,
                        int sus, int particular, int convenio) { }

    public record Dia(LocalDate dia, LocalDateTime momento, String origem, List<Bloco> blocos) { }

    /** disponivel = banco do painel ligado; desde = primeiro dia guardado (de todo o histórico). */
    public record Historico(boolean disponivel, LocalDate desde, List<Dia> dias) { }

    /** Os últimos N dias guardados até ontem (o de hoje é o "agora" da tela), da base pedida, do mais antigo ao mais novo. */
    public Historico ler(String base, int dias, LocalDate hoje) {
        if (!banco.configurado()) return new Historico(false, null, List.of());
        preparar();
        int n = Math.max(1, Math.min(dias, DIAS_MAX));
        LocalDate de = hoje.minusDays(n), ate = hoje.minusDays(1);
        Map<LocalDate, Dia> m = new TreeMap<>();
        banco.jdbc().query("SELECT * FROM OCUPACAO_DIARIA WHERE BASE = ? AND DIA BETWEEN ? AND ? ORDER BY DIA, A_PARTE, GRUPO", r -> {
            LocalDate d = r.getDate("dia").toLocalDate();
            m.computeIfAbsent(d, k -> {
                try {
                    return new Dia(k, r.getTimestamp("momento").toLocalDateTime(), r.getString("origem"), new ArrayList<>());
                } catch (java.sql.SQLException e) {
                    throw new IllegalStateException(e);
                }
            }).blocos().add(new Bloco(r.getString("grupo"), r.getString("bloco"), r.getString("tipo_nome"), r.getBoolean("a_parte"),
                    r.getInt("ativos"), r.getInt("ocupados"), r.getInt("sus"), r.getInt("particular"), r.getInt("convenio")));
        }, base, Date.valueOf(de), Date.valueOf(ate));
        Date desde = banco.jdbc().queryForObject("SELECT MIN(DIA) FROM OCUPACAO_DIARIA WHERE BASE = ?", Date.class, base);
        return new Historico(true, desde == null ? null : desde.toLocalDate(), new ArrayList<>(m.values()));
    }
}
