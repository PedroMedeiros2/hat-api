package br.com.hat.hat_api.spdata.dto;

import java.time.LocalDate;

public record InternacaoDetalhadaDTO(
        Long pront,
        Long reg,
        LocalDate entrada,
        String nome,
        String medicoAssistencial,
        String medicoSolicitante,
        String bloco,
        String convenio,
        String clinica,
        String internacao,
        LocalDate nascimento
) { }