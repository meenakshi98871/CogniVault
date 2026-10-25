
package com.cognivault;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DatabaseConnectionTest {

    @Bean
    ApplicationRunner testDatabaseConnection(JdbcTemplate jdbcTemplate) {
        return args -> {
            String databaseName = jdbcTemplate.queryForObject(
                "SELECT current_database()",
                String.class
            );

            System.out.println(
                "CogniVault connected to PostgreSQL database: "
                + databaseName
            );
        };
    }
}
