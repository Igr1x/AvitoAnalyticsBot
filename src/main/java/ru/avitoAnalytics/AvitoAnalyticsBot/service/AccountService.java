package ru.avitoAnalytics.AvitoAnalyticsBot.service;

import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AccountData;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.User;

import java.util.List;
import java.util.Optional;

public interface AccountService {
    Long saveAccount(AccountData accountData);
    Optional<User> getUser(Long telegramId);
    void deleteById(long accountId);
    List<AccountData> findByUserOwnerId(Long userOwnerId);
    Optional<AccountData> findById(Long id);
    List<AccountData> findAll();
    List<String> findUniqueSheetsRef();
    List<AccountData> findAllBySheetsRef(String sheetsRef);
    Optional<AccountData> findByAccountName(String accountName);
    Optional<AccountData> findByUserId(Long userId);
}
