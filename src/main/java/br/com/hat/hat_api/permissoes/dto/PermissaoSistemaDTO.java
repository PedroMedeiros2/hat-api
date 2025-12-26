package br.com.hat.hat_api.permissoes.dto;

import br.com.hat.hat_api.permissoes.model.PermissaoSistema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissaoSistemaDTO {
    private String codigo;
    private String descricao;
    private Integer ativo;

    public PermissaoSistemaDTO(PermissaoSistema permissao) {
        this.codigo = permissao.getCodigo();
        this.descricao = permissao.getDescricao();

    }
}
