package ru.avitoAnalytics.AvitoAnalyticsBot.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.Ads;

import java.util.List;

@Repository
public interface AdsJdbcRepository {
    void save(List<Ads> adsList);
}
