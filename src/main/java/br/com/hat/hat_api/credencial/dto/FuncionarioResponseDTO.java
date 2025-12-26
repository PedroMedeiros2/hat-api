package br.com.hat.hat_api.credencial.dto;

import br.com.hat.hat_api.permissoes.dto.IndicadorDTO;
import br.com.hat.hat_api.permissoes.dto.PermissaoSistemaDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class FuncionarioResponseDTO {
    private String matricula;
    private String nome;
    private String cpf;
    private List<PermissaoSistemaDTO> permissoesSistema;
    private List<IndicadorDTO> indicadoresVisiveis;
//    private List<PermissaoSistema> todasPermissoesSistema;
//    private List<Indicador> todosIndicadores;
}