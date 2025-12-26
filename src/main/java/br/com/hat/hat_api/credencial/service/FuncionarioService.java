package br.com.hat.hat_api.credencial.service;

import br.com.hat.hat_api.credencial.dto.FuncionarioResponseDTO;
import br.com.hat.hat_api.credencial.model.Funcionarios;
import br.com.hat.hat_api.credencial.repository.FuncionarioRepository;
import br.com.hat.hat_api.permissoes.dto.*;
import br.com.hat.hat_api.permissoes.model.Indicador;
import br.com.hat.hat_api.permissoes.model.PermissaoSistema;
import br.com.hat.hat_api.permissoes.model.PermissaoIndicador;
import br.com.hat.hat_api.permissoes.model.UsuarioPermissaoSistema;
import br.com.hat.hat_api.permissoes.repository.IndicadorRepository;
import br.com.hat.hat_api.permissoes.repository.PermissaoSistemaRepository;
import br.com.hat.hat_api.permissoes.repository.PermissaoIndicadorRepository;
import br.com.hat.hat_api.permissoes.repository.UsuarioPermissaoSistemaRepository;
import br.com.hat.hat_api.permissoes.service.UsuarioPermissaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;
    private final UsuarioPermissaoService usuarioPermissaoService;
    private final IndicadorRepository indicadorRepository;
    private final PermissaoSistemaRepository permissaoRepository;
    private final UsuarioPermissaoSistemaRepository usuarioPermissaoSistemaRepository;
    private final PermissaoIndicadorRepository permissaoIndicadorRepository;

    @Transactional(readOnly = true)
    public List<FuncionarioResponseDTO> buscarAtivosPorMatriculaOuNome(String valor) {
        List<Funcionarios> funcionarios = funcionarioRepository.findAtivosByMatriculaOrNomeContaining(valor);

        return funcionarios.stream().map(f -> {
            List<PermissaoSistemaDTO> permissoesSistema =
                    usuarioPermissaoService.getPermissoesSistema(f.getMatricula());

            List<IndicadorDTO> indicadoresVisiveis =
                    usuarioPermissaoService.getIndicadoresVisiveis(f.getMatricula());

            return FuncionarioResponseDTO.builder()
                    .matricula(f.getMatricula())
                    .nome(f.getNome())
                    .cpf(f.getCpf())
                    .permissoesSistema(permissoesSistema)
                    .indicadoresVisiveis(indicadoresVisiveis)
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FuncionarioResponseDTO buscarAtivoPorMatricula(String valor) {
        Funcionarios funcionario = funcionarioRepository.findByMatricula(valor);

        if (funcionario == null) {
            return null; // ou lançar uma exceção, dependendo da sua regra de negócio
        }

        List<PermissaoSistemaDTO> permissoesSistema =
                usuarioPermissaoService.getPermissoesSistema(funcionario.getMatricula());

        List<IndicadorDTO> indicadoresVisiveis =
                usuarioPermissaoService.getIndicadoresVisiveis(funcionario.getMatricula());

        return FuncionarioResponseDTO.builder()
                .matricula(funcionario.getMatricula())
                .nome(funcionario.getNome())
                .cpf(funcionario.getCpf())
                .permissoesSistema(permissoesSistema)
                .indicadoresVisiveis(indicadoresVisiveis)
                .build();
    }


    @Transactional(readOnly = true)
    public Map<String, Object> listarTodosIndicadoresEPermissoes() {
        List<IndicadorDTO> todosIndicadores =
                indicadorRepository.findAll().stream()
                        .map(IndicadorDTO::new)
                        .toList();

        List<PermissaoSistemaDTO> todasPermissoesSistema =
                permissaoRepository.findAll().stream()
                        .map(PermissaoSistemaDTO::new)
                        .toList();

        return Map.of(
                "indicadores", todosIndicadores,
                "permissoesSistema", todasPermissoesSistema
        );
    }

    @Transactional
    public void atualizarPermissoes(AtualizarPermissoesRequest request) {

        String matricula = request.getMatricula();

        // ================================
        // 1. PERMISSÕES DE SISTEMA
        // ================================
        List<UsuarioPermissaoSistema> permissoesExistentes =
                usuarioPermissaoSistemaRepository.findByMatriculaUsuario(matricula);

        // Mapa para lookup rápido (por código da permissão)
        Map<String, UsuarioPermissaoSistema> mapaPermissoesExistentes =
                permissoesExistentes.stream()
                        .collect(Collectors.toMap(
                                p -> p.getPermissaoSistema().getCodigo(),
                                p -> p,
                                (a, b) -> a // evita erro de duplicidade
                        ));

        if (request.getPermissoesSistema() != null) {

            for (PermissaoSistemaUsuarioDTO dto : request.getPermissoesSistema()) {

                UsuarioPermissaoSistema existente = mapaPermissoesExistentes.get(dto.getCodigo());

                if (existente != null) {
                    // Atualiza apenas o necessário
                    existente.setAtivo(dto.getAtivo());
                    usuarioPermissaoSistemaRepository.save(existente);
                } else {
                    // Busca permissão apenas 1 vez
                    PermissaoSistema permissao = permissaoRepository.findByCodigo(dto.getCodigo())
                            .orElseThrow(() ->
                                    new IllegalArgumentException("Permissão não encontrada: " + dto.getCodigo()));

                    UsuarioPermissaoSistema nova = new UsuarioPermissaoSistema();
                    nova.setMatriculaUsuario(matricula);
                    nova.setPermissaoSistema(permissao);
                    nova.setAtivo(dto.getAtivo());

                    usuarioPermissaoSistemaRepository.save(nova);
                }
            }

            // Remover permissões que não estão mais presentes
            List<UsuarioPermissaoSistema> paraRemover =
                    permissoesExistentes.stream()
                            .filter(p -> request.getPermissoesSistema().stream()
                                    .noneMatch(dto -> dto.getCodigo().equals(p.getPermissaoSistema().getCodigo())))
                            .toList();

            if (!paraRemover.isEmpty()) {
                usuarioPermissaoSistemaRepository.deleteAll(paraRemover);
            }

        } else {
            usuarioPermissaoSistemaRepository.deleteAll(permissoesExistentes);
        }

        // ================================
        // 2. PERMISSÕES DE INDICADORES
        // ================================
        List<PermissaoIndicador> indicadoresExistentes =
                permissaoIndicadorRepository.findByMatriculaUsuario(matricula);

        // Mapa por código
        Map<String, PermissaoIndicador> mapaIndicadoresExistentes =
                indicadoresExistentes.stream()
                        .collect(Collectors.toMap(
                                p -> p.getIndicador().getCodigo(),
                                p -> p,
                                (a, b) -> a
                        ));

        if (request.getIndicadores() != null) {

            for (IndicadorUsuarioDTO dto : request.getIndicadores()) {

                PermissaoIndicador existente = mapaIndicadoresExistentes.get(dto.getCodigo());

                if (existente != null) {
                    existente.setPodeVisualizar(dto.getPodeVisualizar());
                    permissaoIndicadorRepository.save(existente);
                } else {
                    Indicador indicador = indicadorRepository.findByCodigo(dto.getCodigo())
                            .orElseThrow(() ->
                                    new IllegalArgumentException("Indicador não encontrado: " + dto.getCodigo()));

                    PermissaoIndicador nova = new PermissaoIndicador();
                    nova.setMatriculaUsuario(matricula);
                    nova.setIndicador(indicador);
                    nova.setPodeVisualizar(dto.getPodeVisualizar());

                    permissaoIndicadorRepository.save(nova);
                }
            }

            List<PermissaoIndicador> paraRemover =
                    indicadoresExistentes.stream()
                            .filter(p -> request.getIndicadores().stream()
                                    .noneMatch(dto -> dto.getCodigo().equals(p.getIndicador().getCodigo())))
                            .toList();

            if (!paraRemover.isEmpty()) {
                permissaoIndicadorRepository.deleteAll(paraRemover);
            }

        } else {
            permissaoIndicadorRepository.deleteAll(indicadoresExistentes);
        }
    }


}
