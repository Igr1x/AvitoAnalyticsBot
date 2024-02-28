package ru.avitoAnalytics.AvitoAnalyticsBot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AccountData;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<AccountData, Long> {
    Optional<AccountData> findByUserId(Long userId);
}
