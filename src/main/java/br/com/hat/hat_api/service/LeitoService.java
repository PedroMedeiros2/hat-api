package br.com.hat.hat_api.service;

import br.com.hat.hat_api.dto.TaxaOcupacaoDTO;
import br.com.hat.hat_api.repository.LeitoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
