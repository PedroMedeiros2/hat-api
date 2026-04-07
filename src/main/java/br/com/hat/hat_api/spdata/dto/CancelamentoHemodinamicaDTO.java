package br.com.hat.hat_api.spdata.dto;

import java.time.LocalDateTime;

public record CancelamentoHemodinamicaDTO(
        Integer salaAnt,
        String operacao,
        LocalDateTime dataHoraAnt,
        String paciente,
        String conv,
        String justif,
        String procto,
        String crm,
        String nomeProfissional,
        String nomeProcedimento,
        String convenio
) { }
