package br.com.hat.hat_api.permissoes.repository;

import br.com.hat.hat_api.permissoes.model.PermissaoIndicador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissaoIndicadorRepository extends JpaRepository<PermissaoIndicador, Integer> {

    List<PermissaoIndicador> findByMatriculaUsuarioAndPodeVisualizar(String matriculaUsuario, Integer podeVisualizar);
    boolean existsByMatriculaUsuarioAndIndicadorIdAndPodeVisualizar(String matriculaUsuario, Integer indicadorId, Integer podeVisualizar);
    Optional<PermissaoIndicador> findByMatriculaUsuarioAndIndicadorId(String matriculaUsuario, Integer indicadorId);
    List<PermissaoIndicador> findByMatriculaUsuario(String matriculaUsuario);
}
