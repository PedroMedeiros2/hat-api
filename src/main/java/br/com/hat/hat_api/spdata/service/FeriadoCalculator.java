package br.com.hat.hat_api.spdata.service;

import br.com.hat.hat_api.spdata.dto.FeriadoResponse;
import br.com.hat.hat_api.spdata.dto.FeriadoResponse.TipoFeriado;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class FeriadoCalculator {

    public LocalDate calcularPascoa(int ano) {
        int a = ano % 19, b = ano / 100, c = ano % 100;
        int d = b / 4,  e = b % 4,  f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4,  k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int mes = (h + l - 7 * m + 114) / 31;
        int dia = ((h + l - 7 * m + 114) % 31) + 1;
        return LocalDate.of(ano, mes, dia);
    }

    public List<FeriadoResponse> calcularPorAno(int ano) {
        LocalDate pascoa = calcularPascoa(ano);
        List<FeriadoResponse> feriados = new ArrayList<>();

        feriados.add(fixo(ano,  1,  1, "Confraternização Universal",  "Ano Novo"));
        feriados.add(fixo(ano,  4, 21, "Tiradentes",                  "Mártir da Independência"));
        feriados.add(fixo(ano,  5,  1, "Dia do Trabalho",             "Dia Internacional dos Trabalhadores"));
        feriados.add(fixo(ano,  9,  7, "Independência do Brasil",     "Proclamação da Independência em 1822"));
        feriados.add(fixo(ano, 10, 12, "Nossa Senhora Aparecida",     "Padroeira do Brasil"));
        feriados.add(fixo(ano, 11,  2, "Finados",                     "Dia de Finados"));
        feriados.add(fixo(ano, 11, 15, "Proclamação da República",    "Proclamação da República em 1889"));
        feriados.add(fixo(ano, 11, 20, "Consciência Negra",           "Dia da Consciência Negra"));
        feriados.add(fixo(ano, 12, 25, "Natal",                       "Nascimento de Jesus Cristo"));

        feriados.add(movel(pascoa.minusDays(48), "Carnaval",               "Segunda-feira de Carnaval",              TipoFeriado.FACULTATIVO));
        feriados.add(movel(pascoa.minusDays(47), "Carnaval",               "Terça-feira de Carnaval",                TipoFeriado.FACULTATIVO));
        feriados.add(movel(pascoa.minusDays(46), "Quarta-feira de Cinzas", "Meio expediente",                        TipoFeriado.FACULTATIVO));
        feriados.add(movel(pascoa.minusDays(2),  "Sexta-feira Santa",      "Paixão de Cristo",                       TipoFeriado.NACIONAL));
        feriados.add(movel(pascoa,               "Páscoa",                  "Ressurreição de Jesus Cristo",           TipoFeriado.NACIONAL));
        feriados.add(movel(pascoa.plusDays(60),  "Corpus Christi",          "Solenidade do Corpo e Sangue de Cristo", TipoFeriado.FACULTATIVO));

        feriados.add(municipal(ano, 7, 3, "Aniversário da Cidade", "Aniversário de fundação de Montes Claros - MG"));

        feriados.sort(Comparator.comparing(FeriadoResponse::data));
        return feriados;
    }

    private FeriadoResponse fixo(int ano, int mes, int dia, String nome, String descricao) {
        return new FeriadoResponse(nome, LocalDate.of(ano, mes, dia), TipoFeriado.NACIONAL, descricao, false);
    }

    private FeriadoResponse movel(LocalDate data, String nome, String descricao, TipoFeriado tipo) {
        return new FeriadoResponse(nome, data, tipo, descricao, true);
    }

    private FeriadoResponse municipal(int ano, int mes, int dia, String nome, String descricao) {
        return new FeriadoResponse(nome, LocalDate.of(ano, mes, dia), TipoFeriado.MUNICIPAL, descricao, false);
    }
}
