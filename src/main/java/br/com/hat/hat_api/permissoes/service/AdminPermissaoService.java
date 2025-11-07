package br.com.hat.hat_api.permissoes.service;

import br.com.hat.hat_api.permissoes.dto.PermissaoIndicadorRequest;
import br.com.hat.hat_api.permissoes.model.Indicador;
import br.com.hat.hat_api.permissoes.model.PermissaoIndicador;
import br.com.hat.hat_api.permissoes.model.PermissaoSistema;
import br.com.hat.hat_api.permissoes.repository.IndicadorRepository;
import br.com.hat.hat_api.permissoes.repository.PermissaoIndicadorRepository;
import br.com.hat.hat_api.permissoes.repository.PermissaoSistemaRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminPermissaoService {

    @Autowired
    private IndicadorRepository indicadorRepo;

    @Autowired
    private PermissaoSistemaRepository permissaoSistemaRepo;

    @Autowired
    private PermissaoIndicadorRepository permissaoIndicadorRepo;

    public List<Indicador> listarTodosIndicadores() {
        return indicadorRepo.findAll();
    }

    public List<PermissaoSistema> listarTodasPermissoesSistema() {
        return permissaoSistemaRepo.findAll();
    }


    @Transactional
    public PermissaoIndicador salvarPermissaoIndicador(PermissaoIndicadorRequest request) {

        Optional<PermissaoIndicador> optPermissao = permissaoIndicadorRepo
                .findByMatriculaUsuarioAndIndicadorId(request.getMatriculaUsuario(), request.getIndicadorId());

        PermissaoIndicador permissao;
        if (optPermissao.isPresent()) {

            permissao = optPermissao.get();
        } else {
            permissao = new PermissaoIndicador();
            permissao.setMatriculaUsuario(request.getMatriculaUsuario());
            Indicador indicador = indicadorRepo.findById(request.getIndicadorId())
                    .orElseThrow(() -> new EntityNotFoundException("Indicador não encontrado: " + request.getIndicadorId()));
            permissao.setIndicador(indicador);
        }

        permissao.setPodeVisualizar(request.getPodeVisualizar());
        return permissaoIndicadorRepo.save(permissao);
    }
}
