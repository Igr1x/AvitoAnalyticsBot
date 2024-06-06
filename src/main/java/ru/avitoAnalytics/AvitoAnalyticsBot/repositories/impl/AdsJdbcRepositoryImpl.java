package ru.avitoAnalytics.AvitoAnalyticsBot.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.Ads;
import ru.avitoAnalytics.AvitoAnalyticsBot.repositories.AdsJdbcRepository;

import java.util.List;

@RequiredArgsConstructor
@Component
public class AdsJdbcRepositoryImpl implements AdsJdbcRepository {

    private final static String INSERT_SQL = """
            INSERT INTO ads (avito_id, owner_id, cost)
            VALUES (?, ?, ?::numeric)
            """;


    private final JdbcTemplate jdbcTemplate;

    @Override
    public void save(List<Ads> adsList) {
        var list = adsList.stream()
                .map(ad -> new Object[] {
                        ad.getAvitoId(),
                        ad.getOwnerId(),
                        ad.getCost()
                })
                .toList();
        jdbcTemplate.batchUpdate(INSERT_SQL, list);
    }
}
