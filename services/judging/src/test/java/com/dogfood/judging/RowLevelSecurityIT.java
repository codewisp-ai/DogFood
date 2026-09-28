package com.dogfood.judging;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class RowLevelSecurityIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("dogfood")
            .withUsername("dogfood_app")
            .withPassword("dogfood_secret");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        // Disable rabbitmq for tests
        registry.add("spring.rabbitmq.host", () -> "localhost");
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Sql(scripts = {
        "classpath:db/migration/V1__create_judging_schema.sql",
        "classpath:db/migration/V2__enable_rls.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Test
    void testRowLevelSecurityEnforcesIsolation() {
        // This test proves that a judge cannot read another judge's scores, 
        // fulfilling the strict T2 isolation requirement.
        
        UUID judge1 = UUID.randomUUID();
        UUID judge2 = UUID.randomUUID();
        UUID submissionId = UUID.randomUUID();

        // 1. Insert a score as Judge 1
        jdbcTemplate.execute("SET app.current_judge_id = '" + judge1.toString() + "'");
        jdbcTemplate.update(
            "INSERT INTO judging.scores (id, judge_id, submission_id, raw_score) VALUES (?, ?, ?, ?)",
            UUID.randomUUID(), judge1, submissionId, 9.5
        );

        // 2. Query as Judge 1 (Should see 1 row)
        int countJudge1 = jdbcTemplate.queryForObject("SELECT count(*) FROM judging.scores", Integer.class);
        assertEquals(1, countJudge1, "Judge 1 should see their own score");

        // 3. Switch context to Judge 2 and query (Should see 0 rows due to RLS)
        jdbcTemplate.execute("SET app.current_judge_id = '" + judge2.toString() + "'");
        int countJudge2 = jdbcTemplate.queryForObject("SELECT count(*) FROM judging.scores", Integer.class);
        assertEquals(0, countJudge2, "Judge 2 should NOT be able to read Judge 1's scores! RLS failed.");
    }
}
