package ru.avitoAnalytics.AvitoAnalyticsBot.service;

import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AccountData;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.User;

import java.util.Optional;

public interface AccountService {

    Long saveAccount(AccountData accountData);
    Optional<User> getUser(Long telegramId);

}
