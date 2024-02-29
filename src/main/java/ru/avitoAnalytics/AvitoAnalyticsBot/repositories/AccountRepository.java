package ru.avitoAnalytics.AvitoAnalyticsBot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AccountData;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<AccountData, Long> {
    List<AccountData> findByUserOwnerId(Long userOwnerId);
}
