package ru.avitoAnalytics.AvitoAnalyticsBot.service;

import ru.avitoAnalytics.AvitoAnalyticsBot.entity.User;

import java.util.Optional;

public interface UserService {

    User saveUser(User user, String username, Long telegramId);
    boolean existsUser(String telegramId);
    Optional<User> getUser(Long telegramId);

}
