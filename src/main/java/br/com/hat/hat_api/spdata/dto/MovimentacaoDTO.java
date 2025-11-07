package br.com.hat.hat_api.spdata.dto;

import java.time.LocalDate;

public record MovimentacaoDTO(
        LocalDate data,
        String tipoConvenio,
        Integer qtdInternacoes,
        Integer qtdAltas,
        Integer qtdObitos,
        Integer qtdObitos24h
) {}
