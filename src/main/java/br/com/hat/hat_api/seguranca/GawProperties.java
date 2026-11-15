package br.com.hat.hat_api.seguranca;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Login unico do GAW (OpenID Connect).
 *
 * @param issuer    endereco publico do GAW (claim iss dos tokens), ex.: https://192.168.2.221
 * @param jwksUri   chave publica do GAW; vazio = {issuer}/oauth2/jwks
 * @param aplicacao codigo desta aplicacao no catalogo do GAW (claim aud e prefixo das permissoes)
 */
@ConfigurationProperties("hat.gaw")
public record GawProperties(String issuer, String jwksUri, String aplicacao) {

    public GawProperties {
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalStateException("hat.gaw.issuer (GAW_ISSUER) e obrigatorio");
        }
        issuer = issuer.replaceAll("/+$", "");
        jwksUri = jwksUri == null || jwksUri.isBlank() ? issuer + "/oauth2/jwks" : jwksUri;
        aplicacao = aplicacao == null || aplicacao.isBlank() ? "indicadores" : aplicacao;
    }
}
