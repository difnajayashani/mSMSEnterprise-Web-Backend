package lk.mobitel.msmsenterprisewebbeapi.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
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

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "lk.mobitel.msmsenterprisewebbeapi.repository.esms",
        entityManagerFactoryRef = "esmsEntityManagerFactory",
        transactionManagerRef= "esmsTransactionManager"
)
public class ESMSDataSourceConfiguration {
    @Bean
    @Primary
    @ConfigurationProperties("esms.datasource")
    public DataSourceProperties esmsDataSourceProperties() {
        return new DataSourceProperties();
    }
    @Bean
    @Primary
    @ConfigurationProperties("esms.datasource.configuration")
    public DataSource esmsDataSource() {
        return esmsDataSourceProperties().initializeDataSourceBuilder()
                .type(HikariDataSource.class).build();
    }
    @Primary
    @Bean(name = "esmsEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean esmsEntityManagerFactory(EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(esmsDataSource())
                .packages("lk.mobitel.msmsenterprisewebbeapi.model.esms")
                .build();
    }
    @Primary
    @Bean
    public PlatformTransactionManager esmsTransactionManager(
            final @Qualifier("esmsEntityManagerFactory") LocalContainerEntityManagerFactoryBean esmsEntityManagerFactory) {
        return new JpaTransactionManager(esmsEntityManagerFactory.getObject());
    }
}