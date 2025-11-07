package br.com.hat.hat_api.spdata.dto;

import java.util.Date;

public record ExameDTO(
        Long reg,
        Date data,
        String medico,
        String exame,
        String convenio,
        String tipoAtendimento,
        String ato
) {}
