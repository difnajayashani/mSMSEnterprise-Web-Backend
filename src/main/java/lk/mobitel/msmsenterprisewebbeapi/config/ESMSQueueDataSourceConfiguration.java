package lk.mobitel.msmsenterprisewebbeapi.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "lk.mobitel.msmsenterprisewebbeapi.repository.esmsqueue",
        entityManagerFactoryRef = "esmsqueueEntityManagerFactory",
        transactionManagerRef= "esmsqueueTransactionManager"
)
public class ESMSQueueDataSourceConfiguration {
    @Bean
    @ConfigurationProperties("esmsqueue.datasource")
    public DataSourceProperties esmsqueueDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("esmsqueue.datasource.configuration")
    public DataSource esmsqueueDataSource() {
        return esmsqueueDataSourceProperties().initializeDataSourceBuilder()
                .type(HikariDataSource.class).build();
    }

    @Bean(name = "esmsqueueEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean esmsqueueEntityManagerFactory(EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(esmsqueueDataSource())
                .packages("lk.mobitel.msmsenterprisewebbeapi.model.esmsqueue")
                .build();
    }

    @Bean
    public PlatformTransactionManager esmsqueueTransactionManager(
            final @Qualifier("esmsqueueEntityManagerFactory") LocalContainerEntityManagerFactoryBean esmsqueueEntityManagerFactory) {
        return new JpaTransactionManager(esmsqueueEntityManagerFactory.getObject());
    }
}
