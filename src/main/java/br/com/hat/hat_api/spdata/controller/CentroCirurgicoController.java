package br.com.hat.hat_api.spdata.controller;

import br.com.hat.hat_api.spdata.dto.AgendaCirurgicaDTO;
import br.com.hat.hat_api.spdata.dto.CancelamentoCirurgicoDTO;
import br.com.hat.hat_api.spdata.dto.CentroCirurgicoDTO;
import br.com.hat.hat_api.spdata.service.CentroCirurgicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/centro-cirurgico")
@RequiredArgsConstructor
public class CentroCirurgicoController {
    private final CentroCirurgicoService centroCirurgicoService;

    @GetMapping
    public ResponseEntity<?> getCentroCirurgico(
            @RequestParam String dataini,
            @RequestParam String datafim) {
        List<CentroCirurgicoDTO> resultado = centroCirurgicoService.listarCentroCirurgico(dataini, datafim);

        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/cancelamentos")
    public ResponseEntity<?> getCancelamentos(
            @RequestParam String dataini,
            @RequestParam String datafim) {
        List<CancelamentoCirurgicoDTO> resultado = centroCirurgicoService.listarCancelamentos(dataini, datafim);

        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/agenda")
    public ResponseEntity<?> getAgenda(
            @RequestParam String dataini,
            @RequestParam String datafim) {
        List<AgendaCirurgicaDTO> resultado = centroCirurgicoService.listarAgenda(dataini, datafim);

        return ResponseEntity.ok(resultado);
    }
}
