package br.com.hat.hat_api.seguranca;

import jakarta.servlet.DispatcherType;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Seguranca da API: so atende quem entrou pelo login unico do GAW.
 *
 * <ul>
 *   <li>Toda chamada precisa do access token do GAW: assinatura RS256 pela chave publica do GAW,
 *       emissor (iss) e destino (aud = esta aplicacao). Sem token ou token invalido = 401.</li>
 *   <li>Cada indicador tem a sua permissao (claim {@code perms}); sem ela = 403. Ver {@link Permissoes}.</li>
 *   <li>A API nao e chamada pelo navegador (so pelo servidor da tela), entao nao ha CORS.</li>
 * </ul>
 */
@Configuration
@EnableConfigurationProperties(GawProperties.class)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filtros(HttpSecurity http, JwtDecoder gawJwtDecoder) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(rotas -> {
                    // o despacho interno de erro (404, 500) nao e uma rota: deixa o status real chegar
                    rotas.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll();
                    for (Permissoes.Regra regra : Permissoes.REGRAS) {
                        var rota = rotas.requestMatchers(regra.metodo(), regra.rota(), regra.rota() + "/**");
                        if (regra.permissao() == null) rota.authenticated();
                        else rota.hasAuthority(regra.permissao());
                    }
                    rotas.anyRequest().denyAll();
                })
                .oauth2ResourceServer(r -> r.jwt(jwt -> jwt.decoder(gawJwtDecoder).jwtAuthenticationConverter(permissoesDoGaw())));
        return http.build();
    }

    /** A chave publica e buscada no GAW no primeiro token (a API sobe mesmo com o GAW fora). */
    @Bean
    public JwtDecoder gawJwtDecoder(GawProperties gaw) {
        NimbusJwtDecoder decodificador = NimbusJwtDecoder.withJwkSetUri(gaw.jwksUri())
                .jwsAlgorithm(SignatureAlgorithm.RS256)
                .build();
        decodificador.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(gaw.issuer()),
                audiencia(gaw.aplicacao())));
        return decodificador;
    }

    /** As permissoes do GAW (claim perms) viram as authorities, sem prefixo. */
    static JwtAuthenticationConverter permissoesDoGaw() {
        JwtGrantedAuthoritiesConverter perms = new JwtGrantedAuthoritiesConverter();
        perms.setAuthoritiesClaimName("perms");
        perms.setAuthorityPrefix("");
        JwtAuthenticationConverter conversor = new JwtAuthenticationConverter();
        conversor.setJwtGrantedAuthoritiesConverter(perms);
        return conversor;
    }

    static OAuth2TokenValidator<Jwt> audiencia(String aplicacao) {
        OAuth2Error erro = new OAuth2Error("invalid_token", "Token nao foi emitido para " + aplicacao, null);
        return jwt -> jwt.getAudience() != null && jwt.getAudience().contains(aplicacao)
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(erro);
    }
}
