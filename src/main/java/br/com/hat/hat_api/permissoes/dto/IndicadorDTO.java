package br.com.hat.hat_api.permissoes.dto;

import br.com.hat.hat_api.permissoes.model.Indicador;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IndicadorDTO {
    private Integer id;
    private String nome;
    private Integer id_pai;

    public IndicadorDTO(Indicador indicador) {
        this.id = indicador.getId();
        this.nome = indicador.getNome();
        this.id_pai = (indicador.getPai() != null) ? indicador.getPai().getId() : null;
    }
}
