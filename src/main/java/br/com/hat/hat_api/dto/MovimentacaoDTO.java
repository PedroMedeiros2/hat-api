package br.com.hat.hat_api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovimentacaoDTO {
    private LocalDate data;
    private String tipoConvenio;
    private Integer qtdInternacoes;
    private Integer qtdAltas;
    private Integer qtdObitos;
    private Integer qtdObitos24h;
}
