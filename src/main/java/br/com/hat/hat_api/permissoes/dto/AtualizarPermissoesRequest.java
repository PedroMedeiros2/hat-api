package br.com.hat.hat_api.permissoes.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AtualizarPermissoesRequest {

    private String matricula;

    private List<IndicadorUsuarioDTO> indicadores;

    private List<PermissaoSistemaUsuarioDTO> permissoesSistema;
}
