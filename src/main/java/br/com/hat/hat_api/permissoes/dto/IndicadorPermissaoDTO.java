package br.com.hat.hat_api.permissoes.dto;

import br.com.hat.hat_api.permissoes.model.Indicador;
import br.com.hat.hat_api.permissoes.model.PermissaoSistema;

import java.util.List;

public class IndicadorPermissaoDTO {
    private List<Indicador> indicadores;
    private List<PermissaoSistema> permissoes;

    public IndicadorPermissaoDTO(List<Indicador> indicadores, List<PermissaoSistema> permissoes) {
        this.indicadores = indicadores;
        this.permissoes = permissoes;
    }
}
