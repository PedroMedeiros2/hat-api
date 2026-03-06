package br.com.hat.hat_api.spdata.service;

import br.com.hat.hat_api.spdata.dto.AtendimentoDTO;
import br.com.hat.hat_api.spdata.repository.AtendimentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AtendimentoService {

    private final AtendimentoRepository atendimentoRepository;

    public List<AtendimentoDTO> listarAtendimentos(String dataini, String datafim) {

        LocalDateTime inicio = LocalDate.parse(dataini).atStartOfDay();
        LocalDateTime fim = LocalDate.parse(datafim).atTime(23, 59, 59);

        List<Object[]> resultados = atendimentoRepository.findAtendimentos(inicio, fim);

        return resultados.stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    private AtendimentoDTO converterParaDTO(Object[] r) {
        return new AtendimentoDTO(((Number) r[0]).longValue(),
                ((Number) r[1]).intValue(),
                ((Timestamp) r[2]),
                ((String) r[3]),
                (String) r[4],
                (String) r[5],
                (String) r[6],
                (String) r[7],
                (String) r[8],
                (String) r[9],
                (String) r[10]
        );
    }
}
