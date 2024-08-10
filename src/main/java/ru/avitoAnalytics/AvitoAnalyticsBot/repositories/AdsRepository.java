package ru.avitoAnalytics.AvitoAnalyticsBot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AccountData;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.Ads;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdsRepository extends JpaRepository<Ads, Long>, AdsJdbcRepository{
    Optional<Ads> findByAvitoId(long avitoId);

    @Query(value = "select avg(a.cost) from Ads a where a.ownerId = :ownerId and a.closingDate >= :date")
    Optional<BigDecimal> findAvgCostAdsByAccountIdAndDate(AccountData ownerId, LocalDate date);

    @Query(value = "select a from Ads a where a.ownerId = :ownerId and a.closingDate >= :date or a.closingDate is null")
    List<Ads> findAllAdsByAccountIdAndDate(AccountData ownerId, LocalDate date);

    @Query(value = "select avg(a.cost) from Ads a where a.ownerId = :ownerId")
    Optional<BigDecimal> findAvgCostAdsByAccountId(AccountData ownerId);

    @Query(value = "select a from Ads a where a.ownerId = :ownerId and a.pubDate <= :pubDate")
    List<Ads> findAdsFilter(AccountData ownerId, LocalDate pubDate);
}
