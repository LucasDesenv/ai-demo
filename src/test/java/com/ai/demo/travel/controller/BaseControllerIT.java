package com.ai.demo.travel.controller;

import io.zonky.test.db.postgres.junit5.EmbeddedPostgresExtension;
import io.zonky.test.db.postgres.junit5.PreparedDbExtension;
import java.sql.Connection;
import javax.sql.DataSource;
import lombok.NonNull;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.TestPropertySourceUtils;

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(initializers = BaseControllerIT.ControllerTestInitializer.class)
@ExtendWith(SpringExtension.class)
public class BaseControllerIT {

    @RegisterExtension
    public static PreparedDbExtension epg = EmbeddedPostgresExtension.preparedDatabase(BaseControllerIT::initDatabase);

    public static class ControllerTestInitializer
            implements
                ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(@NonNull ConfigurableApplicationContext configurableApplicationContext) {
            String jdbcUrl = String.format("jdbc:postgresql://localhost:%s/%s", epg.getConnectionInfo().getPort(),
                    epg.getConnectionInfo().getDbName());
            String[] propertiesToBeReplaced = new String[]{
                    "spring.datasource.url=" + jdbcUrl,
                    "spring.datasource.username=" + epg.getConnectionInfo().getUser(),
                    "spring.datasource.password="
            };
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    configurableApplicationContext, propertiesToBeReplaced);
        }
    }

    private static void initDatabase(DataSource dataSource) {
        try {
            try (Connection connection = dataSource.getConnection()) {
                if (!connection.isValid(2000)) {
                    throw new IllegalStateException("No valid database connection");
                }
            }

        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
