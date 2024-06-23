package ru.avitoAnalytics.AvitoAnalyticsBot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.Ads;

import java.util.Optional;

@Repository
public interface AdsRepository extends JpaRepository<Ads, Long>, AdsJdbcRepository{
    Optional<Ads> findByAvitoId(long avitoId);
}
