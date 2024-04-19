package ru.avitoAnalytics.AvitoAnalyticsBot.service.impl;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import ru.avitoAnalytics.AvitoAnalyticsBot.AvitoAnalyticsBotApplication;

@SpringBootTest(classes = AvitoAnalyticsBotApplication.class)
@ContextConfiguration(initializers = {FullAdsStatisticServiceImplTest.Initializer.class})
public class FullAdsStatisticServiceImplTest {

    private static final PostgreSQLContainer sqlContainer;

    static {
        sqlContainer = new PostgreSQLContainer("postgres")
                .withDatabaseName("AvitoAnalytics")
                .withUsername("sa")
                .withPassword("sa");
        sqlContainer.start();
    }


    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + sqlContainer.getJdbcUrl(),
                    "spring.datasource.username=" + sqlContainer.getUsername(),
                    "spring.datasource.password=" + sqlContainer.getPassword(),
                    "avito.statistic.max_items_per_request=" + 200
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }


}
