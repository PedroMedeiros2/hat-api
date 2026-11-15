package br.com.hat.hat_api.spdata.controller;

import br.com.hat.hat_api.spdata.dto.BlocoDTO;
import br.com.hat.hat_api.spdata.dto.CensoDTO;
import br.com.hat.hat_api.spdata.service.BlocoService;
import br.com.hat.hat_api.spdata.service.CensoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/censo")
@RequiredArgsConstructor
public class CensoController {
    private final CensoService censoService;
    private final BlocoService blocoService;

    @GetMapping("/todos-blocos")
    public ResponseEntity<?> getCensoTodosBlocos(
            @RequestParam String dataini,
            @RequestParam String datafim,
            @RequestParam(required = false, defaultValue = "1") Integer incluirMesmoDia) {
        List<String> blocos = blocoService.listarBlocosAtivos();

        List<CensoDTO> censo = censoService.listarCensoMultiplosBlocos(
                dataini,
                datafim,
                blocos,
                incluirMesmoDia
        );

        return ResponseEntity.ok(censo);
    }

    @GetMapping
    public ResponseEntity<?> getCenso(
            @RequestParam String dataini,
            @RequestParam String datafim,
            @RequestParam(required = false) String bloco,
            @RequestParam(required = false, defaultValue = "1") Integer incluirMesmoDia) {
        List<CensoDTO> censo = (bloco == null || bloco.isBlank())
                ? censoService.listarCensoMultiplosBlocos(
                        dataini,
                        datafim,
                        blocoService.listarBlocosAtivos(),
                        incluirMesmoDia
                )
                : censoService.listarCenso(
                        dataini,
                        datafim,
                        bloco,
                        incluirMesmoDia
                );

        return ResponseEntity.ok(censo);
    }

    @GetMapping("/blocos")
    public ResponseEntity<?> getBlocos() {
        List<BlocoDTO> blocos = censoService.listarBlocos();

        return ResponseEntity.ok(blocos);
    }
}
