package br.com.hat.hat_api.spdata.service;

import br.com.hat.hat_api.spdata.dto.InternacaoDetalhadaDTO;
import br.com.hat.hat_api.spdata.dto.MovimentacaoDTO;
import br.com.hat.hat_api.spdata.dto.TaxaOcupacaoDTO;
import br.com.hat.hat_api.spdata.repository.LeitoRepository;
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

    public List<TaxaOcupacaoDTO> getOcupacaoByBlocos(List<String> blocos) {
        List<Object[]> result = leitoRepository.getTaxaOcupacaoByBlocos(blocos);

        return result.stream()
                .map(obj -> new TaxaOcupacaoDTO(
                        ((String) obj[0]).trim(),
                        ((Number) obj[1]).intValue(),
                        ((Number) obj[2]).intValue(),
                        ((Number) obj[3]).intValue()
                ))
                .toList();
    }

    public List<MovimentacaoDTO> getMovimentacao(String dataini, String datafim) {
        Map<String, MovimentacaoDTO> resultado = new HashMap<>();

        BiConsumer<List<Object[]>, BiFunction<MovimentacaoDTO, Integer, MovimentacaoDTO>> processa =
                (dados, updater) -> {
                    for (Object[] row : dados) {
                        LocalDate data = ((java.sql.Date) row[0]).toLocalDate();
                        String tipo = (String) row[1];
                        int qtd = ((Number) row[2]).intValue();
                        String chave = data.toString() + "_" + tipo;

                        MovimentacaoDTO dtoAtual = resultado.getOrDefault(chave,
                                new MovimentacaoDTO(data, tipo, 0, 0, 0, 0));

                        MovimentacaoDTO dtoNovo = updater.apply(dtoAtual, qtd);
                        resultado.put(chave, dtoNovo);
                    }
                };

        processa.accept(leitoRepository.findInternacoes(dataini, datafim),
                (dto, qtd) -> new MovimentacaoDTO(dto.data(), dto.tipoConvenio(), qtd, dto.qtdAltas(), dto.qtdObitos(), dto.qtdObitos24h()));

        processa.accept(leitoRepository.findAltas(dataini, datafim),
                (dto, qtd) -> new MovimentacaoDTO(dto.data(), dto.tipoConvenio(), dto.qtdInternacoes(), qtd, dto.qtdObitos(), dto.qtdObitos24h()));

        processa.accept(leitoRepository.findObitos(dataini, datafim),
                (dto, qtd) -> new MovimentacaoDTO(dto.data(), dto.tipoConvenio(), dto.qtdInternacoes(), dto.qtdAltas(), qtd, dto.qtdObitos24h()));

        processa.accept(leitoRepository.findObitos24h(dataini, datafim),
                (dto, qtd) -> new MovimentacaoDTO(dto.data(), dto.tipoConvenio(), dto.qtdInternacoes(), dto.qtdAltas(), dto.qtdObitos(), qtd));

        List<MovimentacaoDTO> lista = new ArrayList<>(resultado.values());
        lista.sort(Comparator.comparing(MovimentacaoDTO::data)
                .thenComparing(MovimentacaoDTO::tipoConvenio));

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
                r[2]  != null ? LocalDate.from(((java.sql.Timestamp) r[2]).toLocalDateTime()) : null,
                r[3]  != null ? String.valueOf(r[3]).trim() : null,
                r[4]  != null ? String.valueOf(r[4]).trim() : null,
                r[5]  != null ? String.valueOf(r[5]).trim() : null,
                r[6]  != null ? String.valueOf(r[6]).trim() : null,
                r[7]  != null ? String.valueOf(r[7]).trim() : null,
                r[8]  != null ? String.valueOf(r[8]).trim() : null,
                r[9]  != null ? String.valueOf(r[9]).trim() : null,
                r[10] != null ? ((java.sql.Date) r[10]).toLocalDate() : null
        );
    }

}
