package br.com.hat.hat_api.spdata.service;

import br.com.hat.hat_api.spdata.dto.ExameDTO;
import br.com.hat.hat_api.spdata.repository.ExameRepository;
import br.com.hat.hat_api.spdata.util.Texto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExameService {

    private final ExameRepository exameRepository;

    public List<ExameDTO> listarExamesCardio(String dataini, String datafim) {
        List<Object[]> resultados = exameRepository.findExamesCardio(dataini, datafim);

        return resultados.stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    public List<ExameDTO> listarExamesImagem(String dataini, String datafim) {
        List<Object[]> resultados = exameRepository.findExamesImagem(dataini, datafim);

        return resultados.stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    private ExameDTO converterParaDTO(Object[] obj) {
        return new ExameDTO(
                ((Number) obj[0]).longValue(),
                (Date) obj[1],
                Texto.limpo(obj[2]),
                Texto.limpo(obj[3]),
                Texto.limpo(obj[4]),
                Texto.limpo(obj[5]),
                Texto.limpo(obj[6]),
                obj[7] != null ? ((Number) obj[7]).intValue() : null,
                Texto.limpo(obj[8]),
                (Date) obj[9]
        );
    }
}