package ru.avitoAnalytics.AvitoAnalyticsBot.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AccountData;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.Ads;
import ru.avitoAnalytics.AvitoAnalyticsBot.repositories.AdsJdbcRepository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class AdsJdbcRepositoryImpl implements AdsJdbcRepository {

    private final static String INSERT_SQL = """
            INSERT INTO ads (avito_id, owner_id, cost, pub_date)
            VALUES (?, ?, ?::numeric, ?);
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
                        ad.getOwnerId().getId(),
                        ad.getCost(),
                        ad.getPubDate()
                })
                .toList();
        jdbcTemplate.batchUpdate(INSERT_SQL, list);
    }

    /*@Override
    public List<Ads> findByAvitoId(List<Long> avitoId) {
        if (avitoId == null || avitoId.isEmpty()) {
            return new ArrayList<>();
        }
        String placeholders = String.join(",", Collections.nCopies(avitoId.size(), "?"));

        String sql = "SELECT * FROM ads WHERE closing_date is null and avito_id IN (" + placeholders + ")";

        Object[] params = avitoId.toArray();

        jdbcTemplate.setFetchSize(100);

        var adsList = jdbcTemplate.queryForList(sql, params);
        List<Ads> listAds = new ArrayList<>();
        for(var ad : adsList) {
            Ads ads = Ads.builder()
                    .avitoId((Long) ad.get("avito_id"))
                    .pubDate(LocalDate.parse(ad.get("pub_date").toString()))
                    .cost((BigDecimal) ad.get("cost"))
                    .build();
            listAds.add(ads);
        }
        return listAds;
    }*/


}
