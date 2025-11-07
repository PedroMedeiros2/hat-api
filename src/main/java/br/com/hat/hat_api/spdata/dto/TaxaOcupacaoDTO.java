package br.com.hat.hat_api.spdata.dto;

public record TaxaOcupacaoDTO(
        String convenio,
        Integer leitosOcupados,
        Integer totalLeitos,
        Integer leitosDisponiveis
) {}
