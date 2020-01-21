package com.kafkamgt.uiapi.config;


import com.kafkamgt.uiapi.helpers.db.rdbms.JdbcDataSourceCondition;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.util.Properties;

@Configuration
@EntityScan
@PropertySource(value= {"classpath:application.properties"})
public class DataSourceConfig {

    private static Logger LOG = LoggerFactory.getLogger(DataSourceConfig.class);

    @Autowired
    Environment environment;

    @Bean(name="dataSource")
    @Conditional(JdbcDataSourceCondition.class)
    public DataSource dataSource() throws PropertyVetoException {
        LOG.info("Into datasource config..");
        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(environment.getProperty("spring.datasource.driver.class"));
        dataSource.setUrl(environment.getProperty("spring.datasource.url"));
        dataSource.setUsername(environment.getProperty("spring.datasource.username"));
        dataSource.setPassword(environment.getProperty("spring.datasource.password"));
        LOG.info("Connecting RDBMS datasource..");
        return dataSource;
    }

    @Bean(name="transactionManager")
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

    private static final String[] ENTITYMANAGER_PACKAGES_TO_SCAN = {"com.kafkamgt.uiapi.dao", "com.kafkamgt.uiapi.dao"};

    @Bean(name="entityManagerFactory")
    @Conditional(JdbcDataSourceCondition.class)
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean() {
            //LOG.info("Into entityManagerFactoryBean config..");
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setJpaVendorAdapter(vendorAdaptor());
        try {
            entityManagerFactoryBean.setDataSource(dataSource());
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }
        entityManagerFactoryBean.setPersistenceProviderClass(HibernatePersistenceProvider.class);
        entityManagerFactoryBean.setPackagesToScan(ENTITYMANAGER_PACKAGES_TO_SCAN);
        entityManagerFactoryBean.setJpaProperties(jpaHibernateProperties());

        return entityManagerFactoryBean;
    }

    private Properties jpaHibernateProperties() {

        Properties properties = new Properties();

        properties.put("hibernate.dialect",environment.getProperty("spring.jpa.properties.hibernate.dialect"));
        return properties;
    }


}