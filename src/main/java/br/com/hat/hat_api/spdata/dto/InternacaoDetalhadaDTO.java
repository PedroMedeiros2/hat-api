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
        LocalDate nascimento,
        /** código do caráter (TBCARATERATEND) e grupo: Eletiva, Urgência ou Não informado */
        Integer codigoCarater,
        String grupoCarater,
        /** CID principal e procedimento da internação */
        String cidCodigo,
        String cidNome,
        String procedimentoCodigo,
        String procedimentoNome
) { }