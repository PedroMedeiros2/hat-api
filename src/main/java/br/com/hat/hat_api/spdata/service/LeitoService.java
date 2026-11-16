package br.com.hat.hat_api.spdata.service;

import br.com.hat.hat_api.spdata.dto.InternacaoDetalhadaDTO;
import br.com.hat.hat_api.spdata.dto.MovimentacaoDTO;
import br.com.hat.hat_api.spdata.dto.TaxaOcupacaoDTO;
import br.com.hat.hat_api.spdata.repository.LeitoRepository;
import br.com.hat.hat_api.spdata.dto.FichaInternacaoDTO;
import br.com.hat.hat_api.spdata.dto.SaidaDTO;
import br.com.hat.hat_api.spdata.dto.LeitoOcupacaoDTO;
import br.com.hat.hat_api.spdata.util.Carater;
import br.com.hat.hat_api.spdata.util.MotivoSaida;
import br.com.hat.hat_api.spdata.util.Texto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeitoService {

    private final LeitoRepository leitoRepository;
    private final CatalogoProcedimentos catalogo;

    public List<TaxaOcupacaoDTO> getOcupacaoByBlocos(List<String> blocos) {
        List<Object[]> result = leitoRepository.getTaxaOcupacaoByBlocos(blocos);

        return result.stream()
                .map(obj -> new TaxaOcupacaoDTO(
                        ((String) obj[0]).trim(),
                        obj[1] != null ? ((String) obj[1]).trim() : null,
                        obj[2] != null ? ((String) obj[2]).trim() : null,
                        ((Number) obj[3]).intValue(),
                        ((Number) obj[4]).intValue(),
                        ((Number) obj[5]).intValue()
                ))
                .toList();
    }

    /** Leitos ativos agora, com o paciente quando ocupado (lista e ficha da Taxa de ocupação). */
    public List<LeitoOcupacaoDTO> getLeitosOcupacao(List<String> blocos) {
        return leitoRepository.findLeitosOcupacao(blocos).stream().map(r -> {
            Integer codigoConvenio = r[12] != null ? ((Number) r[12]).intValue() : null;
            Integer codigoCarater = Carater.codigo(r[16]);
            String cid2 = Texto.limpo(r[19]);
            boolean temCid2 = cid2 != null && !cid2.isEmpty();
            boolean ocupado = r[0] != null;
            return new LeitoOcupacaoDTO(
                    Texto.limpo(r[5]),
                    Texto.limpo(r[22]),
                    Texto.limpo(r[23]),
                    Texto.limpo(r[24]),
                    ocupado ? ((Number) r[0]).longValue() : null,
                    r[1] != null ? ((Number) r[1]).longValue() : null,
                    dataHora(r[2], r[3]),
                    r[4] != null ? ((java.sql.Date) r[4]).toLocalDate() : null,
                    Texto.limpo(r[7]),
                    r[8] != null ? ((java.sql.Date) r[8]).toLocalDate() : null,
                    Texto.limpo(r[9]),
                    Texto.limpo(r[10]),
                    Texto.limpo(r[11]),
                    codigoConvenio,
                    Texto.limpo(r[13]),
                    ocupado ? tipoDoConvenio(codigoConvenio) : null,
                    Texto.limpo(r[14]),
                    Texto.limpo(r[15]),
                    codigoCarater,
                    ocupado ? Carater.grupo(codigoCarater) : null,
                    Texto.limpo(r[17]),
                    Texto.limpo(r[18]),
                    temCid2 ? cid2 : null,
                    temCid2 ? Texto.limpo(r[20]) : null,
                    codigoProcedimento(r[21]),
                    nomeProcedimento(r[21], r[12])
            );
        }).toList();
    }

    /** SUS = convênio 1, Particular = convênio 2, os demais = Convenio (regra do painel). */
    static String tipoDoConvenio(Integer codigo) {
        if (codigo == null) return "Convenio";
        return codigo == 1 ? "SUS" : codigo == 2 ? "Particular" : "Convenio";
    }

    public List<MovimentacaoDTO> getMovimentacao(String dataini, String datafim) {
        Map<String, MovimentacaoDTO> resultado = new HashMap<>();

        BiConsumer<List<Object[]>, BiFunction<MovimentacaoDTO, Integer, MovimentacaoDTO>> processa =
                (dados, updater) -> {
                    for (Object[] row : dados) {
                        LocalDate data = ((java.sql.Date) row[0]).toLocalDate();
                        Integer codigo = row[1] != null ? ((Number) row[1]).intValue() : null;
                        String nome = Texto.limpo(row[2]);
                        int qtd = ((Number) row[3]).intValue();
                        String chave = data + "_" + codigo;

                        MovimentacaoDTO dtoAtual = resultado.getOrDefault(chave,
                                new MovimentacaoDTO(data, tipoDoConvenio(codigo), 0, 0, 0, 0, codigo,
                                        nome != null && !nome.isEmpty() ? nome : "Sem convênio"));

                        resultado.put(chave, updater.apply(dtoAtual, qtd));
                    }
                };

        processa.accept(leitoRepository.findInternacoes(dataini, datafim),
                (d, qtd) -> new MovimentacaoDTO(d.data(), d.tipoConvenio(), qtd, d.qtdAltas(), d.qtdObitos(), d.qtdObitos24h(), d.codigoConvenio(), d.convenio()));
        processa.accept(leitoRepository.findAltas(dataini, datafim),
                (d, qtd) -> new MovimentacaoDTO(d.data(), d.tipoConvenio(), d.qtdInternacoes(), qtd, d.qtdObitos(), d.qtdObitos24h(), d.codigoConvenio(), d.convenio()));
        processa.accept(leitoRepository.findObitos(dataini, datafim),
                (d, qtd) -> new MovimentacaoDTO(d.data(), d.tipoConvenio(), d.qtdInternacoes(), d.qtdAltas(), qtd, d.qtdObitos24h(), d.codigoConvenio(), d.convenio()));
        processa.accept(leitoRepository.findObitos24h(dataini, datafim),
                (d, qtd) -> new MovimentacaoDTO(d.data(), d.tipoConvenio(), d.qtdInternacoes(), d.qtdAltas(), d.qtdObitos(), qtd, d.codigoConvenio(), d.convenio()));

        List<MovimentacaoDTO> lista = new ArrayList<>(resultado.values());
        lista.sort(Comparator.comparing(MovimentacaoDTO::data)
                .thenComparing(MovimentacaoDTO::tipoConvenio)
                .thenComparing(MovimentacaoDTO::convenio));

        return lista;
    }

    public List<InternacaoDetalhadaDTO> getInternacoesDetalhadas(String dataini, String datafim) {

        LocalDateTime inicio = LocalDate.parse(dataini).atStartOfDay();
        LocalDateTime fim    = LocalDate.parse(datafim).atTime(23, 59, 59);

        List<Object[]> resultados = leitoRepository.findInternacoesDetalhadas(inicio, fim);

        return resultados.stream()
                .map(this::converterParaInternacaoDetalhadaDTO)
                .collect(Collectors.toList());
    }

    private InternacaoDetalhadaDTO converterParaInternacaoDetalhadaDTO(Object[] r) {
        return new InternacaoDetalhadaDTO(
                r[0]  != null ? ((Number) r[0]).longValue() : null,
                r[1]  != null ? ((Number) r[1]).longValue() : null,
                r[2]  != null ? ((java.sql.Date) r[2]).toLocalDate() : null,
                r[3]  != null ? String.valueOf(r[3]).trim() : null,
                r[4]  != null ? String.valueOf(r[4]).trim() : null,
                r[5]  != null ? String.valueOf(r[5]).trim() : null,
                r[6]  != null ? String.valueOf(r[6]).trim() : null,
                r[7]  != null ? String.valueOf(r[7]).trim() : null,
                r[8]  != null ? String.valueOf(r[8]).trim() : null,
                r[9]  != null ? String.valueOf(r[9]).trim() : null,
                r[10] != null ? ((java.sql.Date) r[10]).toLocalDate() : null,
                Carater.codigo(r[11]),
                Carater.grupo(Carater.codigo(r[11])),
                Texto.limpo(r[12]),
                Texto.limpo(r[13]),
                codigoProcedimento(r[14]),
                nomeProcedimento(r[14], r[15])
        );
    }


    /** Altas e óbitos caso a caso (lista da Movimentação). */
    public List<SaidaDTO> getSaidas(String dataini, String datafim) {
        List<Object[]> linhas = leitoRepository.findSaidas(dataini, datafim);
        return linhas.stream().map(r -> {
            LocalDateTime entrada = dataHora(r[2], r[3]);
            LocalDateTime alta = dataHora(r[4], r[5]);
            Long minutos = entrada != null && alta != null && temHora(r[3]) && temHora(r[5])
                    ? java.time.Duration.between(entrada, alta).toMinutes() : null;
            Integer codigoConvenio = r[10] != null ? ((Number) r[10]).intValue() : null;
            String motivo = Texto.limpo(r[14]);
            boolean obito = MotivoSaida.obito(motivo);
            return new SaidaDTO(
                    r[0] != null ? ((Number) r[0]).longValue() : null,
                    r[1] != null ? ((Number) r[1]).longValue() : null,
                    entrada,
                    alta,
                    Texto.limpo(r[6]),
                    r[7] != null ? ((java.sql.Date) r[7]).toLocalDate() : null,
                    Texto.limpo(r[8]),
                    Texto.limpo(r[9]),
                    codigoConvenio,
                    Texto.limpo(r[11]),
                    tipoDoConvenio(codigoConvenio),
                    Texto.limpo(r[12]),
                    Texto.limpo(r[13]),
                    Carater.codigo(r[16]),
                    Carater.grupo(Carater.codigo(r[16])),
                    motivo,
                    MotivoSaida.grupo(motivo),
                    Texto.limpo(r[15]),
                    obito,
                    minutos,
                    obito && minutos != null && minutos > 1440,
                    Texto.limpo(r[17]),
                    Texto.limpo(r[18]),
                    codigoProcedimento(r[19]),
                    nomeProcedimento(r[19], r[10])
            );
        }).toList();
    }

    private static boolean temHora(Object hhmm) {
        String h = Texto.limpo(hhmm);
        return h != null && h.matches("\\d{4}") && Integer.parseInt(h.substring(0, 2)) < 24 && Integer.parseInt(h.substring(2)) < 60;
    }

    /** Data (DATE) + hora HHMM; sem hora válida, meia-noite do dia. */
    private static LocalDateTime dataHora(Object data, Object hhmm) {
        if (data == null) return null;
        LocalDate d = ((java.sql.Date) data).toLocalDate();
        if (!temHora(hhmm)) return d.atStartOfDay();
        String h = Texto.limpo(hhmm);
        return d.atTime(Integer.parseInt(h.substring(0, 2)), Integer.parseInt(h.substring(2)));
    }

    /** Ficha de uma internação; vazio se o registro não existe. */
    public java.util.Optional<FichaInternacaoDTO> getFicha(Long reg) {
        List<Object[]> linhas = leitoRepository.findFicha(reg);
        if (linhas.isEmpty()) return java.util.Optional.empty();
        Object[] r = linhas.get(0);
        String cid2 = Texto.limpo(r[12]);
        return java.util.Optional.of(new FichaInternacaoDTO(
                r[0] != null ? ((Number) r[0]).longValue() : null,
                r[1] != null ? ((Number) r[1]).longValue() : null,
                dataHora(r[2], r[3]),
                dataHora(r[4], r[5]),
                r[6] != null ? ((java.sql.Date) r[6]).toLocalDate() : null,
                Texto.limpo(r[7]),
                Texto.limpo(r[8]),
                Texto.limpo(r[9]),
                Texto.limpo(r[10]),
                Texto.limpo(r[11]),
                cid2 == null || cid2.isEmpty() ? null : cid2,
                cid2 == null || cid2.isEmpty() ? null : Texto.limpo(r[13]),
                r[14] != null && !"0".equals(Texto.limpo(r[14])) ? Texto.limpo(r[14]) : null,
                Texto.limpo(r[15]),
                Texto.limpo(r[16]),
                Texto.limpo(r[17]),
                Texto.limpo(r[18])
        ));
    }

    private static String codigoProcedimento(Object proced) {
        String c = Texto.limpo(proced);
        return c == null || c.isEmpty() || "0".equals(c) ? null : c;
    }

    /** Nome do procedimento pelo catálogo em memória (SUS na TBPROSUS, demais na TBPROCTO). */
    private String nomeProcedimento(Object proced, Object convenio) {
        return catalogo.nome(codigoProcedimento(proced), convenio != null && ((Number) convenio).intValue() == 1);
    }
}
