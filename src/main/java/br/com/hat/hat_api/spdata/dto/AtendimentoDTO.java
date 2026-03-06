package br.com.hat.hat_api.spdata.dto;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

public record AtendimentoDTO(

        Long codAtendimento,
        Integer prontuario,
        Timestamp dataHoraEntrada,
        String nomePaciente,
        String unidade,
        String especialidade,
        String consulta,
        String retorno,
        String convenio,
        String converteuInternacao,
        String converteuExame
) { }
