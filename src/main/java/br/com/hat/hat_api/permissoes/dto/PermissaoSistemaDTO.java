package br.com.hat.hat_api.permissoes.dto;

import br.com.hat.hat_api.permissoes.model.PermissaoSistema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissaoSistemaDTO {
    private Integer id;
    private String codigo;
    private String descricao;

    public PermissaoSistemaDTO(PermissaoSistema permissao) {
        this.id = permissao.getId();
        this.codigo = permissao.getCodigo();
        this.descricao = permissao.getDescricao();
    }
}
