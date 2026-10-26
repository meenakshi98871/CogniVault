
package com.cognivault.agent;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class MemoryTableInitializer {

    private final JdbcTemplate jdbcTemplate;

    public MemoryTableInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void createMemoryTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS cognivault_memories (
                    id BIGSERIAL PRIMARY KEY,
                    content TEXT NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """;

        jdbcTemplate.execute(sql);

        System.out.println("CogniVault memory table is ready.");
    }
}
