package br.com.hat.hat_api.painel;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;

/**
 * Banco próprio do painel de indicadores (PostgreSQL, o mesmo servidor do GAW, banco e usuário próprios,
 * como a gestão de painéis e as ferramentas): o cadastro CNES e, depois, as metas. O SPData continua só
 * sendo lido (spring.datasource.spdata). Não é um @Bean de DataSource para não mexer na configuração do SPData.
 *
 * <p>Sem INDICADORES_DB_URL o banco fica desligado: a leitura usa o cadastro inicial que vem com a API e a
 * edição responde 503. Os SQLs servem ao PostgreSQL e ao H2 em modo PostgreSQL (o dos testes).</p>
 */
@Component
public class BancoPainel {

    private final HikariDataSource dataSource;
    private final JdbcTemplate jdbc;

    @Autowired
    public BancoPainel(@Value("${hat.banco.url:}") String url,
                       @Value("${hat.banco.usuario:}") String usuario,
                       @Value("${hat.banco.senha:}") String senha) {
        if (url == null || url.isBlank()) {
            this.dataSource = null;
            this.jdbc = null;
            return;
        }
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(usuario);
        config.setPassword(senha);
        config.setPoolName("HikariPool-Painel");
        config.setMaximumPoolSize(4);
        // a API sobe mesmo com o banco fora; a primeira chamada tenta de novo
        config.setInitializationFailTimeout(-1);
        this.dataSource = new HikariDataSource(config);
        this.jdbc = new JdbcTemplate(dataSource);
    }

    /** Banco em memória para os testes (H2 em modo PostgreSQL, nomes em minúsculas como no PostgreSQL). */
    public static BancoPainel emMemoria(String nome) {
        return new BancoPainel("jdbc:h2:mem:" + nome + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1", "sa", "");
    }

    public boolean configurado() {
        return jdbc != null;
    }

    public JdbcTemplate jdbc() {
        if (jdbc == null) throw new BancoDesligado();
        return jdbc;
    }

    /** INSERT numa tabela com ID gerado: devolve o ID (PostgreSQL e H2). */
    public long inserir(String sql, Object... args) {
        GeneratedKeyHolder chave = new GeneratedKeyHolder();
        jdbc().update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
            for (int i = 0; i < args.length; i++) ps.setObject(i + 1, args[i]);
            return ps;
        }, chave);
        Number id = chave.getKeys() != null && chave.getKeys().get("id") != null ? (Number) chave.getKeys().get("id") : chave.getKey();
        if (id == null) throw new IllegalStateException("O banco não devolveu o ID gerado.");
        return id.longValue();
    }

    @PreDestroy
    public void fechar() {
        if (dataSource != null) dataSource.close();
    }

    /** O banco do painel não foi configurado (INDICADORES_DB_URL vazio). */
    public static class BancoDesligado extends RuntimeException {
        public BancoDesligado() {
            super("O banco do painel não está configurado (INDICADORES_DB_URL). O cadastro CNES está só para leitura.");
        }
    }
}
