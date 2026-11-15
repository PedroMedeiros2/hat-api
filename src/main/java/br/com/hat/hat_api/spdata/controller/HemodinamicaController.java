package br.com.hat.hat_api.spdata.controller;

import br.com.hat.hat_api.spdata.dto.AgendaCirurgicaDTO;
import br.com.hat.hat_api.spdata.dto.CancelamentoCirurgicoDTO;
import br.com.hat.hat_api.spdata.dto.ExameHemodinamicaDTO;
import br.com.hat.hat_api.spdata.dto.HemodinamicaDTO;
import br.com.hat.hat_api.spdata.service.HemodinamicaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/hemodinamica")
@RequiredArgsConstructor
public class HemodinamicaController {
    private final HemodinamicaService hemodinamicaService;

    @GetMapping
    public ResponseEntity<?> getHemodinamica(
            @RequestParam String dataini,
            @RequestParam String datafim) {
        List<HemodinamicaDTO> resultado = hemodinamicaService.listarHemodinamica(dataini, datafim);

        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/exames")
    public ResponseEntity<?> getExamesHemodinamica(
            @RequestParam String dataini,
            @RequestParam String datafim) {
        List<ExameHemodinamicaDTO> resultado = hemodinamicaService.listarExamesHemodinamica(dataini, datafim);

        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/cancelamentos")
    public ResponseEntity<?> getCancelamentosHemodinamica(
            @RequestParam String dataini,
            @RequestParam String datafim) {
        List<CancelamentoCirurgicoDTO> resultado = hemodinamicaService.listarCancelamentos(dataini, datafim);

        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/agenda")
    public ResponseEntity<?> getAgendaHemodinamica(
            @RequestParam String dataini,
            @RequestParam String datafim) {
        List<AgendaCirurgicaDTO> resultado = hemodinamicaService.listarAgenda(dataini, datafim);

        return ResponseEntity.ok(resultado);
    }
}