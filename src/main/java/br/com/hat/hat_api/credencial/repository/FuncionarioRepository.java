package br.com.hat.hat_api.credencial.repository;

import br.com.hat.hat_api.credencial.model.Funcionarios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionarios, String> {
    Optional<Funcionarios> findByMatriculaAndStatus(String matricula, String status);

    Funcionarios findByMatricula(String matricula);

    @Query("SELECT f2 " +
            "FROM Funcionarios f2 " +
            "WHERE f2.status = 'A' " +
            "AND (f2.matricula LIKE CONCAT('%', :valor, '%') " +
            "OR f2.nome LIKE CONCAT('%', :valor, '%'))")
    List<Funcionarios> findAtivosByMatriculaOrNomeContaining(@Param("valor") String valor);
}