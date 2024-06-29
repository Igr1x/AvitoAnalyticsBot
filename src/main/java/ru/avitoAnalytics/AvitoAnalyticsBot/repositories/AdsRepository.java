package ru.avitoAnalytics.AvitoAnalyticsBot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AccountData;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.Ads;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdsRepository extends JpaRepository<Ads, Long>, AdsJdbcRepository{
    Optional<Ads> findByAvitoId(long avitoId);

    @Query(value = "select a from Ads a where a.ownerId = :ownerId and a.closingDate >= :date")
    List<Ads> findAllActiveAdsByAccountId(AccountData ownerId, LocalDate date);
}
