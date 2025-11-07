package br.com.hat.hat_api.credencial.repository;

import br.com.hat.hat_api.credencial.model.Funcionarios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionarios, String> {
    Optional<Funcionarios> findByMatriculaAndStatus(String matricula, String status);
}