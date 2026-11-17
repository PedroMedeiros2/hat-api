package br.com.hat.hat_api.internados;

import br.com.hat.hat_api.spdata.repository.LeitoRepository;
import br.com.hat.hat_api.spdata.service.BlocoService;
import br.com.hat.hat_api.spdata.util.Texto;
import org.springframework.stereotype.Service;

import java.text.Collator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Pacientes internados agora, com filiação, e as contagens por bloco (internados e leitos ativos). */
@Service
public class InternadosService {

    private final LeitoRepository leitos;
    private final BlocoService blocos;

    public InternadosService(LeitoRepository leitos, BlocoService blocos) {
        this.leitos = leitos;
        this.blocos = blocos;
    }

    public Internados.Consulta consultar() {
        return montar(leitos.findInternados(blocos.resolverBlocos(null)), LocalDateTime.now());
    }

    /** Monta a resposta a partir das linhas da consulta (uma por leito ativo; REG nulo = leito livre). */
    static Internados.Consulta montar(List<Object[]> linhas, LocalDateTime agora) {
        Map<String, int[]> porBloco = new LinkedHashMap<>();
        List<Internados.Paciente> pacientes = new ArrayList<>();
        for (Object[] r : linhas) {
            String bloco = Texto.limpo(r[4]);
            int[] c = porBloco.computeIfAbsent(bloco, b -> new int[2]);
            c[1]++;
            if (r[0] == null) continue;
            c[0]++;
            pacientes.add(new Internados.Paciente(
                    ((Number) r[0]).longValue(),
                    r[1] != null ? ((Number) r[1]).longValue() : null,
                    bloco,
                    Texto.limpo(r[5]),
                    Texto.limpo(r[6]),
                    dataHora(r[2], r[3]),
                    Texto.limpo(r[7]),
                    data(r[8]),
                    Texto.limpo(r[9]),
                    vazioNulo(r[10]),
                    vazioNulo(r[11]),
                    Texto.limpo(r[12]),
                    Texto.limpo(r[13]),
                    Texto.limpo(r[14])));
        }
        Collator pt = Collator.getInstance(new Locale("pt", "BR"));
        pt.setStrength(Collator.PRIMARY);
        pacientes.sort(Comparator.comparing((Internados.Paciente p) -> p.nome() == null ? "" : p.nome(), pt));
        List<Internados.Bloco> bs = porBloco.entrySet().stream()
                .map(e -> new Internados.Bloco(e.getKey(), e.getValue()[0], e.getValue()[1])).toList();
        return new Internados.Consulta(agora, bs, pacientes);
    }

    private static String vazioNulo(Object v) {
        String s = Texto.limpo(v);
        return s == null || s.isEmpty() ? null : s;
    }

    private static LocalDate data(Object v) {
        if (v instanceof java.sql.Date d) return d.toLocalDate();
        if (v instanceof LocalDate d) return d;
        return null;
    }

    /** Data + hora HHMM; sem hora válida, meia-noite do dia. */
    static LocalDateTime dataHora(Object dia, Object hhmm) {
        LocalDate d = data(dia);
        if (d == null) return null;
        String h = Texto.limpo(hhmm);
        if (h == null || !h.matches("\\d{4}")) return d.atStartOfDay();
        int hh = Integer.parseInt(h.substring(0, 2)), mm = Integer.parseInt(h.substring(2));
        return hh < 24 && mm < 60 ? d.atTime(hh, mm) : d.atStartOfDay();
    }
}
