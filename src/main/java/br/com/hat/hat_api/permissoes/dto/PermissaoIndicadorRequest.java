package br.com.hat.hat_api.permissoes.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissaoIndicadorRequest {
    private String matriculaUsuario;
    private Integer indicadorId;
    private Integer podeVisualizar;
}
