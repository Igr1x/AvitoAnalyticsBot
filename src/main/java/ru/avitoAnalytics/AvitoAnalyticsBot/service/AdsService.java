package ru.avitoAnalytics.AvitoAnalyticsBot.service;

import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AccountData;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.Ads;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AdsService {
    Optional<BigDecimal> findCostByAvitoId(Long avitoId);
    void save(Ads item);
    void save(List<Ads> adsList);
    Optional<BigDecimal> findAvgCostAdsByAccountIdAndDate(AccountData ownerId, LocalDate date);
    Optional<BigDecimal> findAvgCostAdsByAccountId(AccountData ownerId);
    List<Ads> findAllAdsByAccountIdAndDate(AccountData ownerId, LocalDate date);
    List<Ads> findByAvitoId(List<Long> avitoIds);
}
