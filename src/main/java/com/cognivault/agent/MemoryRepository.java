
package com.cognivault.agent;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MemoryRepository {

    private final JdbcTemplate jdbcTemplate;

    public MemoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveMemory(String summary) {
        String sql = """
                INSERT INTO cognivault_memories (content)
                VALUES (?)
                """;

        jdbcTemplate.update(sql, summary);
    }
    
    public java.util.List<java.util.Map<String, Object>> getAllMemories() {
        String sql = """
                SELECT id, content, created_at
                FROM cognivault_memories
                ORDER BY created_at DESC
                """;

        return jdbcTemplate.queryForList(sql);
    }

}
