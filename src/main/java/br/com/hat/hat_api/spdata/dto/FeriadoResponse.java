package br.com.hat.hat_api.spdata.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record FeriadoResponse(
        String nome,
        @JsonFormat(pattern = "dd/MM/yyyy") LocalDate data,
        TipoFeriado tipo,
        String descricao,
        boolean movel
) {
    public enum TipoFeriado {
        NACIONAL, FACULTATIVO, MUNICIPAL
    }
}
