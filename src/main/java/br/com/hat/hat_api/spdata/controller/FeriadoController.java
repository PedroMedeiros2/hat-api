package br.com.hat.hat_api.spdata.controller;

import br.com.hat.hat_api.spdata.dto.FeriadoResponse;
import br.com.hat.hat_api.spdata.service.FeriadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/feriados")
@RequiredArgsConstructor
public class FeriadoController {

    private final FeriadoService service;

    @GetMapping("/ano/{ano}")
    public ResponseEntity<List<FeriadoResponse>> buscarPorAno(@PathVariable int ano) {
        return ResponseEntity.ok(service.buscarPorAno(ano));
    }

    @GetMapping("/ano/{ano}/mes/{mes}")
    public ResponseEntity<List<FeriadoResponse>> buscarPorAnoEMes(
            @PathVariable int ano, @PathVariable int mes) {
        return ResponseEntity.ok(service.buscarPorAnoEMes(ano, mes));
    }

    @GetMapping("/periodo")
    public ResponseEntity<List<FeriadoResponse>> buscarPorPeriodo(
            @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate inicio,
            @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fim) {
        return ResponseEntity.ok(service.buscarPorPeriodo(inicio, fim));
    }

    @GetMapping("/pascoa/{ano}")
    public ResponseEntity<Map<String, Object>> calcularPascoa(@PathVariable int ano) {
        LocalDate pascoa = service.calcularPascoa(ano);
        return ResponseEntity.ok(Map.of(
                "ano",              ano,
                "pascoa",           pascoa.toString(),
                "carnaval_segunda", pascoa.minusDays(48).toString(),
                "carnaval_terca",   pascoa.minusDays(47).toString(),
                "sextaSanta",       pascoa.minusDays(2).toString(),
                "corpusChristi",    pascoa.plusDays(60).toString()
        ));
    }

    @GetMapping("/proximos")
    public ResponseEntity<List<FeriadoResponse>> buscarProximos(
            @RequestParam(defaultValue = "5") int quantidade) {
        return ResponseEntity.ok(service.buscarProximos(quantidade));
    }

    @GetMapping("/verificar")
    public ResponseEntity<Map<String, Object>> verificarFeriado(
            @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate data) {
        return ResponseEntity.ok(Map.of(
                "data",      data.toString(),
                "ehFeriado", service.ehFeriado(data)
        ));
    }
}
