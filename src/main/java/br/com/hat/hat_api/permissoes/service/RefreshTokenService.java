package br.com.hat.hat_api.permissoes.service;

import br.com.hat.hat_api.permissoes.model.RefreshToken;
import br.com.hat.hat_api.permissoes.repository.RefreshTokenRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;


@Service
public class RefreshTokenService {

    @Getter
    @Value("${app.jwt.refresh-expiration-ms}")
    private Long refreshTokenDurationMs;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional("permissoesTransactionManager")
    public RefreshToken createRefreshToken(String matricula) {
        refreshTokenRepository.deleteByMatriculaUsuarioAndDataExpiracaoBefore(matricula, Instant.now());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setMatriculaUsuario(matricula);
        refreshToken.setDataExpiracao(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional("permissoesTransactionManager")
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getDataExpiracao().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token expirado. Por favor, faça login novamente.");
        }
        return token;
    }

}
