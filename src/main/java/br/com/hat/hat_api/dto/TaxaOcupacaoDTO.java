package br.com.hat.hat_api.dto;

public record TaxaOcupacaoDTO(
        String convenio,
        Integer leitosOcupados,
        Integer totalLeitos,
        Integer leitosDisponiveis
) {}
