package br.com.hat.hat_api.service;

import br.com.hat.hat_api.dto.MovimentacaoDTO;
import br.com.hat.hat_api.dto.TaxaOcupacaoDTO;
import br.com.hat.hat_api.repository.LeitoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.function.BiConsumer;

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

        BiConsumer<List<Object[]>, BiConsumer<MovimentacaoDTO, Integer>> processa = (dados, setter) -> {
            for (Object[] row : dados) {
                LocalDate data = ((java.sql.Date) row[0]).toLocalDate();
                String tipo = (String) row[1];
                int qtd = ((Number) row[2]).intValue();
                
                String chave = data.toString() + "_" + tipo;

                MovimentacaoDTO dto = resultado.get(chave);
                if (dto == null) {
                    dto = new MovimentacaoDTO();
                    dto.setData(data);
                    dto.setTipoConvenio(tipo);
                    dto.setQtdInternacoes(0);
                    dto.setQtdAltas(0);
                    dto.setQtdObitos(0);
                    dto.setQtdObitos24h(0);
                    resultado.put(chave, dto);
                }

                setter.accept(dto, qtd);
            }
        };

        processa.accept(leitoRepository.findInternacoes(dataini, datafim), (dto, qtd) -> dto.setQtdInternacoes(qtd));
        processa.accept(leitoRepository.findAltas(dataini, datafim),        (dto, qtd) -> dto.setQtdAltas(qtd));
        processa.accept(leitoRepository.findObitos(dataini, datafim),       (dto, qtd) -> dto.setQtdObitos(qtd));
        processa.accept(leitoRepository.findObitos24h(dataini, datafim),    (dto, qtd) -> dto.setQtdObitos24h(qtd));

        List<MovimentacaoDTO> lista = new ArrayList<>(resultado.values());
        lista.sort(Comparator.comparing(MovimentacaoDTO::getData)
                .thenComparing(MovimentacaoDTO::getTipoConvenio));

        return lista;
    }



}
