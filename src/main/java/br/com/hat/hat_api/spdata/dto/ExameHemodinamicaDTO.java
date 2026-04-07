package br.com.hat.hat_api.spdata.dto;

import java.time.LocalDate;

public record ExameHemodinamicaDTO(
        String pront,
        Long reg,
        LocalDate data,
        String medico_solicitante,
        String medico_executante,
        String exame,
        String convenio,
        String tipoAtendimento,
        String ato
) { }
