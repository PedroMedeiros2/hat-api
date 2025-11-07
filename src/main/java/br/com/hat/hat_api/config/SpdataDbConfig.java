package br.com.hat.hat_api.config;

import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "br.com.hat.hat_api.spdata.repository",
        entityManagerFactoryRef = "spdataEntityManagerFactory",
        transactionManagerRef = "spdataTransactionManager"
)
public class SpdataDbConfig {
    @Bean(name = "spdataDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.spdata")
    public DataSource spdataDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "spdataEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean spdataEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("spdataDataSource") DataSource dataSource) {

        Map<String, String> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.community.dialect.FirebirdDialect");
        properties.put("hibernate.hbm2ddl.auto", "none");

        return builder
                .dataSource(dataSource)
                // Pacote das @Entity deste banco
                .packages("br.com.hat.hat_api.spdata.model")
                .persistenceUnit("spdata")
                .properties(properties)
                .build();
    }

    @Bean(name = "spdataTransactionManager")
    public PlatformTransactionManager spdataTransactionManager(
            @Qualifier("spdataEntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory.getObject());
    }
}