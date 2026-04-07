package br.com.hat.hat_api.spdata.dto;

import java.time.LocalDate;

public record CentroCirurgicoDTO(
        Long procto,
        LocalDate data,
        Long reg,
        Long seq,
        Long cirur,
        String outTipAnes,
        Long idCirur,
        Long sala,
        Long esp,
        String nomeEsp,
        String nomeCirurgiao,
        String anestesia,
        String nomeProcedimento,
        String nomePaciente,
        Long codConv,
        String convenio,
        String horaInicio,
        String horaTermino,
        Long duracaoMinutos,
        String tipoCirurgia,
        String urgencia,
        String nomeAnestesista
) { }
