package br.com.hat.hat_api.permissoes.repository;

import br.com.hat.hat_api.permissoes.model.UsuarioPermissaoSistema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioPermissaoSistemaRepository extends JpaRepository<UsuarioPermissaoSistema, Integer> {

    List<UsuarioPermissaoSistema> findByMatriculaUsuario(String matriculaUsuario);

    boolean existsByMatriculaUsuarioAndPermissaoSistemaId(String matriculaUsuario, Integer permissaoId);
}
