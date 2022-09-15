package com.kafkamgt.uiapi.config;

import com.kafkamgt.uiapi.helpers.db.rdbms.JdbcDataSourceCondition;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

@Configuration
@EntityScan
@PropertySource(value = {"classpath:application.properties"})
@Slf4j
public class DataSourceConfig {

  @Autowired Environment environment;

  @Bean(name = "dataSource")
  @Conditional(JdbcDataSourceCondition.class)
  public DataSource dataSource() throws SQLException {
    log.info("Into Hikari datasource config.");
    final HikariDataSource dataSource = new HikariDataSource();
    dataSource.setDriverClassName(environment.getProperty("spring.datasource.driver.class"));
    dataSource.setJdbcUrl(environment.getProperty("spring.datasource.url"));
    dataSource.setUsername(environment.getProperty("spring.datasource.username"));
    dataSource.setPassword(environment.getProperty("spring.datasource.password"));
    dataSource.setAutoCommit(true);
    dataSource.setConnectionTimeout(
        Long.parseLong(
            Objects.requireNonNull(
                environment.getProperty("spring.datasource.hikari.connectionTimeout"))));
    dataSource.setIdleTimeout(
        Long.parseLong(
            Objects.requireNonNull(
                environment.getProperty("spring.datasource.hikari.idleTimeout"))));
    dataSource.setLoginTimeout(60);
    dataSource.setMinimumIdle(100);
    dataSource.setMaximumPoolSize(
        Integer.parseInt(
            Objects.requireNonNull(
                environment.getProperty("spring.datasource.hikari.maxPoolSize"))));

    log.info("Connecting to RDBMS datasource.");
    return dataSource;
  }

  @Bean(name = "transactionManager")
  @Conditional(JdbcDataSourceCondition.class)
  public JpaTransactionManager jpaTransactionManager() {
    JpaTransactionManager transactionManager = new JpaTransactionManager();
    transactionManager.setEntityManagerFactory(entityManagerFactoryBean().getObject());
    return transactionManager;
  }

  private HibernateJpaVendorAdapter vendorAdaptor() {
    HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
    vendorAdapter.setShowSql(true);
    return vendorAdapter;
  }

  private static final String[] ENTITYMANAGER_PACKAGES_TO_SCAN = {
    "com.kafkamgt.uiapi.dao", "com.kafkamgt.uiapi.dao"
  };

  @Bean(name = "entityManagerFactory")
  @Conditional(JdbcDataSourceCondition.class)
  public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean() {
    LocalContainerEntityManagerFactoryBean entityManagerFactoryBean =
        new LocalContainerEntityManagerFactoryBean();
    entityManagerFactoryBean.setJpaVendorAdapter(vendorAdaptor());
    try {
      entityManagerFactoryBean.setDataSource(dataSource());
    } catch (SQLException e) {
      log.error("Exception: ", e);
    }
    entityManagerFactoryBean.setPersistenceProviderClass(HibernatePersistenceProvider.class);
    entityManagerFactoryBean.setPackagesToScan(ENTITYMANAGER_PACKAGES_TO_SCAN);
    entityManagerFactoryBean.setJpaProperties(jpaHibernateProperties());

    return entityManagerFactoryBean;
  }

  private Properties jpaHibernateProperties() {
    Properties properties = new Properties();
    properties.put(
        "hibernate.dialect", environment.getProperty("spring.jpa.properties.hibernate.dialect"));
    properties.put("hibernate.show_sql", environment.getProperty("spring.jpa.hibernate.show_sql"));
    properties.put(
        "hibernate.generate-ddl", environment.getProperty("spring.jpa.hibernate.generate-ddl"));
    properties.put("hibernate.ddl-auto", environment.getProperty("spring.jpa.hibernate.ddl-auto"));
    properties.put(
        "hibernate.jdbc.lob.non_contextual_creation",
        environment.getProperty("spring.jpa.hibernate.jdbc.lob.non_contextual_creation"));

    return properties;
  }
}
