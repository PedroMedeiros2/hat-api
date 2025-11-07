package br.com.hat.hat_api.permissoes.repository;

import br.com.hat.hat_api.permissoes.model.PermissaoSistema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissaoSistemaRepository extends JpaRepository<PermissaoSistema, Integer> {
}
