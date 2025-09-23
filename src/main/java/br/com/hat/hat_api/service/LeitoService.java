package br.com.hat.hat_api.service;

import br.com.hat.hat_api.dto.MovimentacaoDTO;
import br.com.hat.hat_api.dto.TaxaOcupacaoDTO;
import br.com.hat.hat_api.repository.LeitoRepository;
import org.springframework.stereotype.Service;

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

        // Função para processar cada consulta individualmente
        BiConsumer<List<Object[]>, BiConsumer<MovimentacaoDTO, Integer>> processa = (dados, setter) -> {
            for (Object[] row : dados) {
                String tipo = (String) row[0];
                int qtd = ((Number) row[1]).intValue();

                // Se ainda não existe DTO para esse tipo, cria e adiciona
                MovimentacaoDTO dto = resultado.get(tipo);
                if (dto == null) {
                    dto = new MovimentacaoDTO();
                    dto.setTipoConvenio(tipo);
                    dto.setQtdInternacoes(0);
                    dto.setQtdAltas(0);
                    dto.setQtdObitos(0);
                    dto.setQtdObitos24h(0);
                    resultado.put(tipo, dto);
                }

                // Aplica o valor no campo correspondente
                setter.accept(dto, qtd);
            }
        };

        // Processa cada uma das 4 consultas separadas
        processa.accept(leitoRepository.findInternacoes(dataini, datafim), (dto, qtd) -> dto.setQtdInternacoes(qtd));
        processa.accept(leitoRepository.findAltas(dataini, datafim),        (dto, qtd) -> dto.setQtdAltas(qtd));
        processa.accept(leitoRepository.findObitos(dataini, datafim),       (dto, qtd) -> dto.setQtdObitos(qtd));
        processa.accept(leitoRepository.findObitos24h(dataini, datafim),    (dto, qtd) -> dto.setQtdObitos24h(qtd));

        // Calcula o total geral
        MovimentacaoDTO total = new MovimentacaoDTO();
        total.setTipoConvenio("TOTAL");
        total.setQtdInternacoes(0);
        total.setQtdAltas(0);
        total.setQtdObitos(0);
        total.setQtdObitos24h(0);

        for (MovimentacaoDTO dto : resultado.values()) {
            total.setQtdInternacoes(total.getQtdInternacoes() + dto.getQtdInternacoes());
            total.setQtdAltas(total.getQtdAltas() + dto.getQtdAltas());
            total.setQtdObitos(total.getQtdObitos() + dto.getQtdObitos());
            total.setQtdObitos24h(total.getQtdObitos24h() + dto.getQtdObitos24h());
        }

        // Retorna os dados ordenados + total
        List<MovimentacaoDTO> lista = new ArrayList<>(resultado.values());
        lista.sort(Comparator.comparing(MovimentacaoDTO::getTipoConvenio));
        lista.add(total);

        return lista;
    }


}
