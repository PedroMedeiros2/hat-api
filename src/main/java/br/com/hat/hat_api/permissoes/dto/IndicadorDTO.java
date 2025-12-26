package br.com.hat.hat_api.permissoes.dto;

import br.com.hat.hat_api.permissoes.model.Indicador;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IndicadorDTO {
    private Integer id;
    private String codigo;
    private String nome;
    private String descricao;
    private Integer idPai;
    private String codigoPai;

    public IndicadorDTO(Indicador indicador) {
        this.id = indicador.getId();
        this.codigo = indicador.getCodigo();
        this.nome = indicador.getNome();
        this.descricao = indicador.getDescricao();

        if (indicador.getPai() != null) {
            this.idPai = indicador.getPai().getId();
            this.codigoPai = indicador.getPai().getCodigo();
        }
    }
}
