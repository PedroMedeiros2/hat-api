package br.com.hat.hat_api.spdata.service;

import br.com.hat.hat_api.spdata.dto.BlocoDTO;
import br.com.hat.hat_api.spdata.dto.CensoDTO;
import br.com.hat.hat_api.spdata.repository.CensoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CensoService {

    private final CensoRepository censoRepository;

    public List<CensoDTO> listarCenso(String dataini, String datafim, String blocoSelecionado, Integer incluirMesmoDia) {

        LocalDateTime inicio = LocalDate.parse(dataini).atStartOfDay();
        LocalDateTime fim = LocalDate.parse(datafim).atTime(23, 59, 59);

        List<Object[]> resultados = censoRepository.findCenso(inicio, fim, blocoSelecionado, incluirMesmoDia);

        return resultados.stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    public List<CensoDTO> listarCensoMultiplosBlocos(
            String dataini,
            String datafim,
            List<String> blocos,
            Integer incluirMesmoDia) {

        return blocos.parallelStream()
                .flatMap(bloco ->
                        listarCenso(dataini, datafim, bloco, incluirMesmoDia).stream()
                )
                .collect(Collectors.toList());
    }

    private CensoDTO converterParaDTO(Object[] r) {
        return new CensoDTO(
                (String) r[0],
                ((Number) r[1]).longValue(),
                ((Number) r[2]).longValue(),
                (String) r[3],
                (String) r[4],
                (String) r[5],
                (String) r[6],
                r[7] != null ? ((java.sql.Date) r[7]).toLocalDate() : null
        );
    }

    public List<BlocoDTO> listarBlocos() {
        List<Object[]> resultados = censoRepository.findBlocos();

        return resultados.stream()
                .map(this::converterParaBlocoDTO)
                .collect(Collectors.toList());
    }

    private BlocoDTO converterParaBlocoDTO(Object[] r) {
        return new BlocoDTO(
                (String) r[0],
                ((Number) r[1]).longValue()
        );
    }
}
