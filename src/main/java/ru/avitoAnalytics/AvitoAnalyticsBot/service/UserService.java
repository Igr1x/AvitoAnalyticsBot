package ru.avitoAnalytics.AvitoAnalyticsBot.service;

import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AccountData;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.User;
import ru.avitoAnalytics.AvitoAnalyticsBot.exceptions.RepeatAccountDataException;

import java.util.Optional;

public interface UserService {

    User saveUser(User user);
    Optional<User> getUser(Long telegramId);
    User addAccount(User user, AccountData accountData) throws RepeatAccountDataException;

}
