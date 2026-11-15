package br.com.hat.hat_api.spdata.controller;

import br.com.hat.hat_api.spdata.dto.AtendimentoDTO;
import br.com.hat.hat_api.spdata.service.AtendimentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/atendimentos")
@RequiredArgsConstructor
public class AtendimentoController {
    private final AtendimentoService atendimentoService;

    @GetMapping
    public ResponseEntity<?> getAtendimentos(
            @RequestParam String dataini,
            @RequestParam String datafim) {
        List<AtendimentoDTO> atendimentos =
                atendimentoService.listarAtendimentos(dataini, datafim);

        return ResponseEntity.ok(atendimentos);
    }
}
