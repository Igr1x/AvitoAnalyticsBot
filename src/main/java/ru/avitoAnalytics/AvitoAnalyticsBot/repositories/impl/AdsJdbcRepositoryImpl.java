package ru.avitoAnalytics.AvitoAnalyticsBot.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.Ads;
import ru.avitoAnalytics.AvitoAnalyticsBot.repositories.AdsJdbcRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class AdsJdbcRepositoryImpl implements AdsJdbcRepository {

    private final static String INSERT_SQL = """
            INSERT INTO ads (avito_id, owner_id, cost)
            VALUES (?, ?, ?::numeric)
            """;
    private final static String SELECT_SQL = """
            SELECT avito_id, closing_date FROM ads
            WHERE avito_id = ?;
            """;

    private static final class AdsMapper implements RowMapper<Ads> {
        @Override
        public Ads mapRow(ResultSet rs, int rowNum) throws SQLException {
            Ads ads = new Ads();
            ads.setId(rs.getLong("id"));
            ads.setClosingDate(rs.getDate("closind_date").toLocalDate());
            return ads;
        }
    }


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

    @Override
    public List<Ads> findByAvitoId(List<Long> avitoId) {
        String placeholders = String.join(",", avitoId.stream().map(id -> "?").toArray(String[]::new));
        String sql = "SELECT * FROM ads WHERE avito_id IN (" + placeholders + ")";
        Object[] params = avitoId.toArray();
        jdbcTemplate.setFetchSize(100);
        List<Ads> adsList = jdbcTemplate.query(sql, new AdsMapper());
        return adsList;
    }
}
