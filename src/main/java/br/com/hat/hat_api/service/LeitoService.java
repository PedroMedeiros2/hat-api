package br.com.hat.hat_api.service;

import br.com.hat.hat_api.dto.MovimentacaoDTO;
import br.com.hat.hat_api.dto.TaxaOcupacaoDTO;
import br.com.hat.hat_api.repository.LeitoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

@Service
public class LeitoService {

    private final LeitoRepository leitoRepository;

    public LeitoService(LeitoRepository leitoRepository) {
        this.leitoRepository = leitoRepository;
    }

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



}
