package com.userservice.config;

import com.userservice.circuitbreaker.CircuitBreaker;
import com.userservice.discovery.ServiceDiscoveryClient;
import com.userservice.discovery.HeartbeatScheduler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableWebMvc
@EnableTransactionManagement
@EnableAspectJAutoProxy
@EnableJpaRepositories(basePackages = "com.userservice.repository")
@ComponentScan(basePackages = "com.userservice")
@PropertySource("classpath:application.properties")
public class AppConfig {
    
    private final Environment env;
    private final ExternalConfig externalConfig;
    
    public AppConfig(Environment env, ExternalConfig externalConfig) {
        this.env = env;
        this.externalConfig = externalConfig;
    }
    
    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(externalConfig.getDbDriver());
        dataSource.setUrl(externalConfig.getDbUrl());
        dataSource.setUsername(externalConfig.getDbUsername());
        dataSource.setPassword(externalConfig.getDbPassword());
        return dataSource;
    }
    
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan("com.userservice.entity");
        
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        
        Properties properties = new Properties();
        properties.setProperty("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto", "update"));
        properties.setProperty("hibernate.dialect", env.getProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect"));
        properties.setProperty("hibernate.show_sql", env.getProperty("hibernate.show_sql", "true"));
        properties.setProperty("hibernate.format_sql", env.getProperty("hibernate.format_sql", "true"));
        
        em.setJpaProperties(properties);
        return em;
    }
    
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
    }
    
    // ========== SERVICE DISCOVERY BEANS ==========
    // Регистрация сервиса в discovery-server при старте
    // Heartbeat каждые 10 секунд для подтверждения активности

    @Bean
    public ServiceDiscoveryClient serviceDiscoveryClient() {
        return new ServiceDiscoveryClient(
            externalConfig.getDiscoveryUrl(),
            externalConfig.getServiceName(),
            externalConfig.getServiceHost(),
            externalConfig.getServerPort()
        );
    }
    
    @Bean
    public HeartbeatScheduler heartbeatScheduler(ServiceDiscoveryClient discoveryClient) {
        return new HeartbeatScheduler(discoveryClient);
    }
    
    // ========== CIRCUIT BREAKER BEANS ==========
    // Защита вызовов notification-service от отказов
    // При 3 ошибках -> OPEN на 30 секунд -> fallback через Kafka
    
    @Bean
    public CircuitBreaker notificationServiceCircuitBreaker() {
        return new CircuitBreaker(
            externalConfig.getNotificationCircuitThreshold(),
            externalConfig.getNotificationCircuitTimeout(),
            externalConfig.getNotificationCircuitHalfOpen()
        );
    }
}