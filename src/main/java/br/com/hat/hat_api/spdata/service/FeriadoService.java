package br.com.hat.hat_api.spdata.service;

import br.com.hat.hat_api.spdata.dto.FeriadoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class FeriadoService {

    private final FeriadoCalculator calculator;

    public List<FeriadoResponse> buscarPorAno(int ano) {
        return calculator.calcularPorAno(ano);
    }

    public List<FeriadoResponse> buscarPorAnoEMes(int ano, int mes) {
        return buscarPorAno(ano).stream()
                .filter(f -> f.data().getMonthValue() == mes)
                .toList();
    }

    public List<FeriadoResponse> buscarPorPeriodo(LocalDate inicio, LocalDate fim) {
        if (inicio.isAfter(fim)) {
            throw new IllegalArgumentException(
                    "A data inicial (" + inicio + ") deve ser anterior ou igual à data final (" + fim + ").");
        }
        return IntStream.rangeClosed(inicio.getYear(), fim.getYear())
                .mapToObj(calculator::calcularPorAno)
                .flatMap(List::stream)
                .filter(f -> !f.data().isBefore(inicio) && !f.data().isAfter(fim))
                .sorted(Comparator.comparing(FeriadoResponse::data))
                .toList();
    }

    public List<FeriadoResponse> buscarProximos(int quantidade) {
        LocalDate hoje = LocalDate.now();
        List<FeriadoResponse> candidatos = new ArrayList<>(calculator.calcularPorAno(hoje.getYear()));
        candidatos.addAll(calculator.calcularPorAno(hoje.getYear() + 1));
        return candidatos.stream()
                .filter(f -> !f.data().isBefore(hoje))
                .sorted(Comparator.comparing(FeriadoResponse::data))
                .limit(quantidade)
                .toList();
    }

    public LocalDate calcularPascoa(int ano) {
        return calculator.calcularPascoa(ano);
    }

    public boolean ehFeriado(LocalDate data) {
        return buscarPorAno(data.getYear()).stream()
                .anyMatch(f -> f.data().equals(data));
    }
}