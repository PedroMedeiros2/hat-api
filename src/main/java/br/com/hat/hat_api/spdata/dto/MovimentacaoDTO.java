package br.com.hat.hat_api.spdata.dto;

import java.time.LocalDate;

/**
 * Movimentação de um dia e um convênio. tipoConvenio agrupa (SUS = código 1, Particular = código 2,
 * os demais = Convenio); convenio e codigoConvenio são o convênio real (campos acrescentados, os
 * antigos continuam iguais).
 */
public record MovimentacaoDTO(
        LocalDate data,
        String tipoConvenio,
        Integer qtdInternacoes,
        Integer qtdAltas,
        Integer qtdObitos,
        Integer qtdObitos24h,
        Integer codigoConvenio,
        String convenio
) {}
