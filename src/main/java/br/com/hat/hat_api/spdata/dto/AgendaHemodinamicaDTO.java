package br.com.hat.hat_api.spdata.dto;

import java.time.LocalDateTime;

public record AgendaHemodinamicaDTO(
        Integer sala,
        LocalDateTime dataHora,
        String justif,
        String pront,
        String procto,
        String situacao
) { }
