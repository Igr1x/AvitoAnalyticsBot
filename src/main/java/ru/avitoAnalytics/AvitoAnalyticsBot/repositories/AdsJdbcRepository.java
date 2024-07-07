package ru.avitoAnalytics.AvitoAnalyticsBot.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.Ads;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdsJdbcRepository {
    void save(List<Ads> adsList);
    //List<Ads> findByAvitoId(List<Long> avitoId);
}
