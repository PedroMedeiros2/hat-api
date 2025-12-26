package br.com.hat.hat_api.permissoes.service;

import br.com.hat.hat_api.permissoes.dto.IndicadorPermissaoDTO;
import br.com.hat.hat_api.permissoes.model.Indicador;
import br.com.hat.hat_api.permissoes.model.PermissaoSistema;
import br.com.hat.hat_api.permissoes.repository.IndicadorRepository;
import br.com.hat.hat_api.permissoes.repository.PermissaoSistemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IndicadorPermissaoService {

    @Autowired
    private IndicadorRepository indicadorRepository;

    @Autowired
    private PermissaoSistemaRepository permissaoSistemaRepository;

    public IndicadorPermissaoDTO getIndicadoresEPermissoes() {
        List<Indicador> indicadores = indicadorRepository.findAll();

        List<PermissaoSistema> permissoes = permissaoSistemaRepository.findAll();

        return new IndicadorPermissaoDTO(indicadores, permissoes);
    }
}
