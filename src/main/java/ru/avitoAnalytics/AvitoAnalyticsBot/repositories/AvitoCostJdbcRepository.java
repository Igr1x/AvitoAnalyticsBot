package ru.avitoAnalytics.AvitoAnalyticsBot.repositories;

import org.springframework.stereotype.Repository;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AvitoCost;

import java.util.List;
import java.util.Optional;

@Repository
public interface AvitoCostJdbcRepository {
    Optional<AvitoCost> findAvitoCost(String region, String city, String address,
                                      String category, String subcategory, String param);
}
