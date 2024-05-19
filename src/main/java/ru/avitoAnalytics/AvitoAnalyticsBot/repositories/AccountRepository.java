package ru.avitoAnalytics.AvitoAnalyticsBot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AccountData;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<AccountData, Long> {
    List<AccountData> findByUserOwnerId(Long userOwnerId);
    List<AccountData> findAllBySheetsRef(String sheetsRef);
    Optional<AccountData> findAllByAccountName(String accountName);

    @Query(value = "select distinct a.sheetsRef from AccountData a")
    List<String> findUniqueSheetsRef();
}
