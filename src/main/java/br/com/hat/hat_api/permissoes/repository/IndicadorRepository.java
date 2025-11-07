package br.com.hat.hat_api.permissoes.repository;

import br.com.hat.hat_api.permissoes.model.Indicador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IndicadorRepository extends JpaRepository<Indicador, Integer> {
}
