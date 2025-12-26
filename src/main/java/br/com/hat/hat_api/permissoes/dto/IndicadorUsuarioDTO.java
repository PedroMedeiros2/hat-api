package br.com.hat.hat_api.permissoes.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IndicadorUsuarioDTO {
    private String codigo;
    private Integer podeVisualizar;

    public IndicadorUsuarioDTO(String codigo, Integer podeVisualizar) {
        this.codigo = codigo;
        this.podeVisualizar = podeVisualizar;
    }
}
