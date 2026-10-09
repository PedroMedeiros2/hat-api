package br.com.hat.hat_api.spdata.dto;

public record TaxaOcupacaoDTO(
        String bloco,
        String nome,
        String convenio,
        Integer leitosOcupados,
        Integer totalLeitos,
        Integer leitosDisponiveis
) {}
