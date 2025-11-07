package br.com.hat.hat_api.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
        basePackages = "br.com.hat.hat_api.credencial.repository",
        entityManagerFactoryRef = "credencialEntityManagerFactory",
        transactionManagerRef = "credencialTransactionManager"
)
public class CredencialDbConfig {

    @Primary
    @Bean(name = "credencialDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.credencial")
    public DataSource credencialDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "credencialEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean credencialEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("credencialDataSource") DataSource dataSource) {

        Map<String, Object> properties = new HashMap<>();

        properties.put("hibernate.dialect", "org.hibernate.dialect.MariaDBDialect");

        properties.put("hibernate.hbm2ddl.auto", "none");

        properties.put("hibernate.physical_naming_strategy",
                "org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl");

        return builder
                .dataSource(dataSource)
                .packages("br.com.hat.hat_api.credencial.model")
                .persistenceUnit("credencial")
                .properties(properties)
                .build();
    }

    @Primary
    @Bean(name = "credencialTransactionManager")
    public PlatformTransactionManager credencialTransactionManager(
            @Qualifier("credencialEntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory.getObject());
    }
}
