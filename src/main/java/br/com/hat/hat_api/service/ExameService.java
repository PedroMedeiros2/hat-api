package br.com.hat.hat_api.service;

import br.com.hat.hat_api.dto.ExameDTO;
import br.com.hat.hat_api.repository.ExameRepository;
import jakarta.transaction.Transactional;
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
                (String) obj[2],
                (String) obj[3],
                (String) obj[4],
                (String) obj[5],
                (String) obj[6]
        );
    }
}