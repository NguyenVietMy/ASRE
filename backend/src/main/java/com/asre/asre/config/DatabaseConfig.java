package com.asre.asre.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    // Primary datasource: Supabase (metadata)
    @Primary
    @Bean(name = "supabaseDataSource")
    public DataSource supabaseDataSource(
            @Value("${spring.datasource.primary.url}") String url,
            @Value("${spring.datasource.primary.username}") String username,
            @Value("${spring.datasource.primary.password}") String password,
            @Value("${spring.datasource.primary.driver-class-name}") String driverClassName) {
        return DataSourceBuilder.create()
                .url(url)
                .username(username)
                .password(password)
                .driverClassName(driverClassName)
                .build();
    }

    // Secondary datasource: TimescaleDB (time-series)
    @Bean(name = "timescaledbDataSource")
    public DataSource timescaledbDataSource(
            @Value("${spring.datasource.timescaledb.url}") String url,
            @Value("${spring.datasource.timescaledb.username}") String username,
            @Value("${spring.datasource.timescaledb.password}") String password,
            @Value("${spring.datasource.timescaledb.driver-class-name}") String driverClassName) {
        return DataSourceBuilder.create()
                .url(url)
                .username(username)
                .password(password)
                .driverClassName(driverClassName)
                .build();
    }

    // Primary Flyway: Supabase migrations
    @Primary
    @Bean(name = "supabaseFlyway")
    public Flyway supabaseFlyway(@Qualifier("supabaseDataSource") DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .validateOnMigrate(true)
                .load();
        flyway.migrate();
        return flyway;
    }

    // Secondary Flyway: TimescaleDB migrations
    @Bean(name = "timescaledbFlyway")
    public Flyway timescaledbFlyway(@Qualifier("timescaledbDataSource") DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration-timescaledb")
                .baselineOnMigrate(true)
                .validateOnMigrate(true)
                .load();
        flyway.migrate();
        return flyway;
    }

    // JdbcTemplate for Supabase
    @Primary
    @Bean(name = "supabaseJdbcTemplate")
    public JdbcTemplate supabaseJdbcTemplate(@Qualifier("supabaseDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    // JdbcTemplate for TimescaleDB
    @Bean(name = "timescaledbJdbcTemplate")
    public JdbcTemplate timescaledbJdbcTemplate(@Qualifier("timescaledbDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
