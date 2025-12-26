package br.com.hat.hat_api.permissoes.repository;

import br.com.hat.hat_api.permissoes.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByMatriculaUsuarioAndDataExpiracaoBefore(String matriculaUsuario, Instant now);
}
