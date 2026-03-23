package br.com.hat.hat_api.spdata.dto;

import java.time.LocalDate;

public record CensoDTO(
        String tipoEvento,
        Long reg,
        Long pront,
        String bloco,
        String acomod,
        String convenio,
        String especialidade,
        LocalDate dataReferencia
) { }
