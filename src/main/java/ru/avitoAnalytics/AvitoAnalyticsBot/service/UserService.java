package ru.avitoAnalytics.AvitoAnalyticsBot.service;

import ru.avitoAnalytics.AvitoAnalyticsBot.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    User saveUser(User user);
    List<User> getAllUser();
    Optional<User> getUser(Long telegramId);
    void updateUserData(User user);
    void extensionRate(User user);
}
