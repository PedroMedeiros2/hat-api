package br.com.hat.hat_api.spdata.service;

import br.com.hat.hat_api.spdata.dto.AgendaCirurgicaDTO;
import br.com.hat.hat_api.spdata.dto.CancelamentoCirurgicoDTO;
import br.com.hat.hat_api.spdata.dto.ExameHemodinamicaDTO;
import br.com.hat.hat_api.spdata.dto.HemodinamicaDTO;
import br.com.hat.hat_api.spdata.repository.HemodinamicaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HemodinamicaService {

    private final HemodinamicaRepository hemodinamicaRepository;

    public List<HemodinamicaDTO> listarHemodinamica(String dataini, String datafim) {

        LocalDate inicio = LocalDate.parse(dataini);
        LocalDate fim = LocalDate.parse(datafim);

        ExecutorService executor = Executors.newFixedThreadPool(10);

        try {
            List<CompletableFuture<List<HemodinamicaDTO>>> futures = inicio.datesUntil(fim.plusDays(1))
                    .map(dia -> CompletableFuture.supplyAsync(() ->
                                    hemodinamicaRepository.findHemodinamica(dia, dia)
                                            .stream()
                                            .map(this::converterParaDTO)
                                            .collect(Collectors.toList()),
                            executor))
                    .toList();

            return futures.stream()
                    .flatMap(f -> f.join().stream())
                    .sorted(Comparator
                            .comparing(HemodinamicaDTO::data,
                                    Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(HemodinamicaDTO::horaInicio,
                                    Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(HemodinamicaDTO::horaTermino,
                                    Comparator.nullsLast(Comparator.naturalOrder())))
                    .collect(Collectors.toList());
        } finally {
            executor.shutdown();
        }
    }

    private HemodinamicaDTO converterParaDTO(Object[] r) {
        return new HemodinamicaDTO(
                r[0] != null ? ((Number) r[0]).longValue() : null,
                r[1] != null ? ((java.sql.Date) r[1]).toLocalDate() : null,
                r[2] != null ? ((Number) r[2]).longValue() : null,
                r[3] != null ? ((Number) r[3]).longValue() : null,
                r[4] != null ? ((Number) r[4]).longValue() : null,
                r[5] != null ? r[5].toString() : null,
                r[6] != null ? ((Number) r[6]).longValue() : null,
                r[7] != null ? ((Number) r[7]).longValue() : null,
                r[8] != null ? ((Number) r[8]).longValue() : null,
                r[9] != null ? r[9].toString() : null,
                r[10] != null ? r[10].toString() : null,
                r[11] != null ? r[11].toString() : null,
                r[12] != null ? r[12].toString() : null,
                r[13] != null ? r[13].toString() : null,
                r[14] != null ? ((Number) r[14]).longValue() : null,
                r[15] != null ? r[15].toString() : null,
                r[16] != null ? r[16].toString() : null,
                r[17] != null ? r[17].toString() : null,
                r[18] != null ? ((Number) r[18]).longValue() : null,
                r[19] != null ? r[19].toString() : null,
                r[20] != null ? r[20].toString() : null,
                r[21] != null ? r[21].toString() : null
        );
    }

    public List<ExameHemodinamicaDTO> listarExamesHemodinamica(String dataini, String datafim) {

        LocalDate inicio = LocalDate.parse(dataini);
        LocalDate fim = LocalDate.parse(datafim);

        ExecutorService executor = Executors.newFixedThreadPool(10);

        try {
            List<CompletableFuture<List<ExameHemodinamicaDTO>>> futures = inicio.datesUntil(fim.plusDays(1))
                    .map(dia -> CompletableFuture.supplyAsync(() ->
                                    hemodinamicaRepository.findExamesHemodinamica(dia, dia)
                                            .stream()
                                            .map(this::converterParaExameDTO)
                                            .collect(Collectors.toList()),
                            executor))
                    .toList();

            return futures.stream()
                    .flatMap(f -> f.join().stream())
                    .sorted(Comparator
                            .comparing(ExameHemodinamicaDTO::data,
                                    Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(ExameHemodinamicaDTO::pront,
                                    Comparator.nullsLast(Comparator.naturalOrder())))
                    .collect(Collectors.toList());
        } finally {
            executor.shutdown();
        }
    }

    private ExameHemodinamicaDTO converterParaExameDTO(Object[] r) {
        return new ExameHemodinamicaDTO(
                r[0] != null ? String.valueOf(r[0]).trim() : null,
                r[1] != null ? ((Number) r[1]).longValue() : null,
                r[2] != null ? ((java.sql.Date) r[2]).toLocalDate() : null,
                r[3] != null ? String.valueOf(r[3]).trim() : null,
                r[4] != null ? String.valueOf(r[4]).trim() : null,
                r[5] != null ? String.valueOf(r[5]).trim() : null,
                r[6] != null ? String.valueOf(r[6]).trim() : null,
                r[7] != null ? String.valueOf(r[7]).trim() : null,
                r[8] != null ? String.valueOf(r[8]).trim() : null
        );
    }

    public List<CancelamentoCirurgicoDTO> listarCancelamentos(String dataini, String datafim) {

        LocalDate inicio = LocalDate.parse(dataini);
        LocalDate fim = LocalDate.parse(datafim);

        ExecutorService executor = Executors.newFixedThreadPool(10);

        try {
            List<CompletableFuture<List<CancelamentoCirurgicoDTO>>> futures = inicio.datesUntil(fim.plusDays(1))
                    .map(dia -> CompletableFuture.supplyAsync(() -> {
                        LocalDateTime inicioDia = dia.atStartOfDay();
                        LocalDateTime fimDia = dia.atTime(23, 59, 59);
                        return hemodinamicaRepository.findCancelamentosHemodinamica(inicioDia, fimDia)
                                .stream()
                                .map(this::converterParaCancelamentoDTO)
                                .collect(Collectors.toList());
                    }, executor))
                    .toList();

            return futures.stream()
                    .flatMap(f -> f.join().stream())
                    .sorted(Comparator
                            .comparing(CancelamentoCirurgicoDTO::dataHoraAnt,
                                    Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(CancelamentoCirurgicoDTO::salaAnt,
                                    Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(CancelamentoCirurgicoDTO::crm,
                                    Comparator.nullsLast(Comparator.naturalOrder())))
                    .collect(Collectors.toList());
        } finally {
            executor.shutdown();
        }
    }

    private CancelamentoCirurgicoDTO converterParaCancelamentoDTO(Object[] r) {
        return new CancelamentoCirurgicoDTO(
                r[0] != null ? Integer.valueOf(((Number) r[0]).intValue()) : null,
                r[1] != null ? String.valueOf(r[1]).trim() : null,
                r[2] != null ? ((java.sql.Timestamp) r[2]).toLocalDateTime() : null,
                r[3] != null ? String.valueOf(r[3]).trim() : null,
                r[4] != null ? String.valueOf(r[4]).trim() : null,
                r[5] != null ? String.valueOf(r[5]).trim() : null,
                r[6] != null ? String.valueOf(r[6]).trim() : null,
                r[7] != null ? String.valueOf(r[7]).trim() : null,
                r[8] != null ? String.valueOf(r[8]).trim() : null,
                r[9] != null ? String.valueOf(r[9]).trim() : null,
                r[10] != null ? String.valueOf(r[10]).trim() : null
        );
    }

    public List<AgendaCirurgicaDTO> listarAgenda(String dataini, String datafim) {

        LocalDate inicio = LocalDate.parse(dataini);
        LocalDate fim = LocalDate.parse(datafim);

        ExecutorService executor = Executors.newFixedThreadPool(10);

        try {
            List<CompletableFuture<List<AgendaCirurgicaDTO>>> futures = inicio.datesUntil(fim.plusDays(1))
                    .map(dia -> CompletableFuture.supplyAsync(() -> {
                        LocalDateTime inicioDia = dia.atStartOfDay();
                        LocalDateTime fimDia = dia.atTime(23, 59, 59);
                        return hemodinamicaRepository.findAgendaHemodinamica(inicioDia, fimDia)
                                .stream()
                                .map(this::converterParaAgendaDTO)
                                .collect(Collectors.toList());
                    }, executor))
                    .toList();

            return futures.stream()
                    .flatMap(f -> f.join().stream())
                    .sorted(Comparator
                            .comparing(AgendaCirurgicaDTO::dataHora,
                                    Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(AgendaCirurgicaDTO::sala,
                                    Comparator.nullsLast(Comparator.naturalOrder())))
                    .collect(Collectors.toList());
        } finally {
            executor.shutdown();
        }
    }

    private AgendaCirurgicaDTO converterParaAgendaDTO(Object[] r) {
        return new AgendaCirurgicaDTO(
                r[0] != null ? Integer.valueOf(((Number) r[0]).intValue()) : null,
                r[1] != null ? ((java.sql.Timestamp) r[1]).toLocalDateTime() : null,
                r[2] != null ? String.valueOf(r[2]).trim() : null,
                r[3] != null ? String.valueOf(r[3]).trim() : null,
                r[4] != null ? String.valueOf(r[4]).trim() : null,
                r[5] != null ? String.valueOf(r[4]).trim() : null
        );
    }
}