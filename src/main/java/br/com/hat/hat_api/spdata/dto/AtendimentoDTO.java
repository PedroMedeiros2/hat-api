package br.com.hat.hat_api.spdata.dto;

import java.util.Date;

public record AtendimentoDTO(
        Long reg,
        Date data,
        Character tipoAtendimento,
        String local,
        String especialidade,
        String retorno,
        String convenio
) { }
