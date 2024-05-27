package ru.avitoAnalytics.AvitoAnalyticsBot.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AvitoCost;
import ru.avitoAnalytics.AvitoAnalyticsBot.repositories.AvitoCostJdbcRepository;
import java.sql.ResultSet;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class AvitoCostJdbcRepositoryImpl implements AvitoCostJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String SELECT_SQL = """
            SELECT * FROM avito_cost
            WHERE (region = ? OR region = ? OR region = ?)
                AND category = ?
                AND subcategory = ?
                AND (subcategory1 = ? OR subcategory2 = ? OR
                     subcategory3 = ? OR subcategory4 = ? OR subcategory5 = ?)
            """;


    @Override
    public Optional<AvitoCost> findAvitoCost(String region, String city, String address,
                                             String category, String subcategory, String param) {
        return jdbcTemplate.query(SELECT_SQL, (ResultSet rs) -> {
                    if (rs.next()) {
                        AvitoCost avitoCost = new AvitoCost();
                        avitoCost.setCost(rs.getBigDecimal("cost"));
                        return Optional.of(avitoCost);
                    } else {
                        return Optional.empty();
                    }
                },
                region, city, address, category, subcategory, param, param, param, param, param
        );
    }
}
