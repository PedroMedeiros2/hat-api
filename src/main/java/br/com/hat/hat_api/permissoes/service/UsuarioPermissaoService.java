package br.com.hat.hat_api.permissoes.service;

import br.com.hat.hat_api.permissoes.dto.IndicadorDTO;
import br.com.hat.hat_api.permissoes.dto.PermissaoSistemaDTO;
import br.com.hat.hat_api.permissoes.model.PermissaoIndicador;
import br.com.hat.hat_api.permissoes.model.UsuarioPermissaoSistema;
import br.com.hat.hat_api.permissoes.repository.PermissaoIndicadorRepository;
import br.com.hat.hat_api.permissoes.repository.UsuarioPermissaoSistemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioPermissaoService {

    @Autowired
    private PermissaoIndicadorRepository permissaoIndicadorRepo;

    @Autowired
    private UsuarioPermissaoSistemaRepository usuarioPermissaoSistemaRepo;

    public List<IndicadorDTO> getIndicadoresVisiveis(String matricula) {
        List<PermissaoIndicador> permissoes = permissaoIndicadorRepo
                .findByMatriculaUsuarioAndPodeVisualizar(matricula, 1);

        return permissoes.stream()
                .map(permissao -> new IndicadorDTO(permissao.getIndicador()))
                .collect(Collectors.toList());
    }


    public List<PermissaoSistemaDTO> getPermissoesSistema(String matricula) {
        List<UsuarioPermissaoSistema> relacoes = usuarioPermissaoSistemaRepo
                .findByMatriculaUsuario(matricula);

        return relacoes.stream()
                .map(relacao -> new PermissaoSistemaDTO(relacao.getPermissaoSistema()))
                .collect(Collectors.toList());
    }
}
