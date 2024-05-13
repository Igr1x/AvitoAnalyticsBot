/*
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import ru.avitoAnalytics.AvitoAnalyticsBot.AvitoAnalyticsBotApplication;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.Items;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.StatisticAvitoService;

import java.util.List;

@SpringBootTest(classes = AvitoAnalyticsBotApplication.class)
@ContextConfiguration(initializers = {TestStatisticAvito.Initializer.class})
public class TestStatisticAvito {

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


    @Autowired
    private StatisticAvitoService statisticAvitoService;

    @Test
    public void getStatisticTest() {
        List<Long> itemIds = List.of(3697630326L, 3697628099L, 3697624252L, 3697624199L, 3697609532L, 3697586930L, 3697576886L, 3697551744L, 3697546038L);
        String token = statisticAvitoService.getToken("qOQ2nw84qbT-oHkpNf8B", "ixU_7Qcz6FLRwJuI6lWnNnbhUDbLkxBvkIPykHd8");
        List<Items> items = statisticAvitoService.getStatistic(itemIds, token, "327144371", "2023-07-01", "2024-01-01").stream().filter(items1 -> !items1.getStats().isEmpty()).toList();
    }

}
*/
