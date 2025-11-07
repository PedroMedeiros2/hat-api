package br.com.hat.hat_api.spdata.controller;

import br.com.hat.hat_api.spdata.dto.ExameDTO;
import br.com.hat.hat_api.spdata.service.ExameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/exames")
@RequiredArgsConstructor
public class ExameController {
    private final ExameService exameService;

    @GetMapping("/cardiologia")
    public ResponseEntity<List<ExameDTO>> getExamesCardio(
            @RequestParam String dataini,
            @RequestParam String datafim) {

        List<ExameDTO> exames = exameService.listarExamesCardio(dataini, datafim);
        return ResponseEntity.ok(exames);
    }

    @GetMapping("/imagem")
    public ResponseEntity<List<ExameDTO>> getExamesImagem(
            @RequestParam String dataini,
            @RequestParam String datafim) {

        List<ExameDTO> exames = exameService.listarExamesImagem(dataini, datafim);
        return ResponseEntity.ok(exames);
    }
}
