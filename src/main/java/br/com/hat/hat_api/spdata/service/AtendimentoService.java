package br.com.hat.hat_api.spdata.service;

import br.com.hat.hat_api.spdata.dto.AtendimentoDTO;
import br.com.hat.hat_api.spdata.repository.AtendimentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AtendimentoService {

    private final AtendimentoRepository atendimentoRepository;

    public List<AtendimentoDTO> listarAtendimentos(String dataini, String datafim) {

        LocalDate inicio = LocalDate.parse(dataini);
        LocalDate fim = LocalDate.parse(datafim);

        ExecutorService executor = Executors.newFixedThreadPool(10);

        try {
            List<CompletableFuture<List<AtendimentoDTO>>> futures = inicio.datesUntil(fim.plusDays(1))
                    .map(dia -> CompletableFuture.supplyAsync(() -> {
                        LocalDateTime inicioDia = dia.atStartOfDay();
                        LocalDateTime fimDia = dia.atTime(23, 59, 59);
                        return atendimentoRepository.findAtendimentos(inicioDia, fimDia)
                                .stream()
                                .map(this::converterParaDTO)
                                .collect(Collectors.toList());
                    }, executor))
                    .toList();

            return futures.stream()
                    .flatMap(f -> f.join().stream())
                    .collect(Collectors.toList());
        } finally {
            executor.shutdown();
        }
    }

    private AtendimentoDTO converterParaDTO(Object[] r) {
        return new AtendimentoDTO(
                ((Number) r[0]).longValue(),
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