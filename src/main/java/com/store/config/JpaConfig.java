package com.store.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@ComponentScan("com.store")
public class JpaConfig {
    @Bean
    public DataSource dataSource() {
    var ds = new DriverManagerDataSource();
    ds.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
    ds.setUrl("jdbc:sqlserver://localhost:1433;databaseName=shop;encrypt=true;trustServerCertificate=true");
    ds.setUsername("sa");
    ds.setPassword(System.getenv("DB_PASSWORD"));
    return ds;
}
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource ds) {
        var emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(ds);
        emf.setPackagesToScan("com.store.model");
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Properties p = new Properties();
        p.put("hibernate.hbm2ddl.auto", "update");
        p.put("hibernate.show_sql", "true");
        p.put("hibernate.generate_statistics", "true");   // used in Part 6
        emf.setJpaProperties(p);
        return emf;
    }
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
    @Bean
    public static  PersistenceExceptionTranslationPostProcessor translator() {
        return new PersistenceExceptionTranslationPostProcessor();
    }
}