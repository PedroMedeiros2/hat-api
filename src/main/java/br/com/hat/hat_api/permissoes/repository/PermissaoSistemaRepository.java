package br.com.hat.hat_api.permissoes.repository;

import br.com.hat.hat_api.permissoes.model.PermissaoSistema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissaoSistemaRepository extends JpaRepository<PermissaoSistema, Integer> {
    Optional<PermissaoSistema> findByCodigo(String codigo);
}
