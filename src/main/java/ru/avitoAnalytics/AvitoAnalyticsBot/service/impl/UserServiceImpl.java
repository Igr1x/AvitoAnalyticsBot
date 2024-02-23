package ru.avitoAnalytics.AvitoAnalyticsBot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.User;
import ru.avitoAnalytics.AvitoAnalyticsBot.repositories.UserRepository;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.UserService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public boolean existsUser(String telegramId) {
        return userRepository.existsByTelegramId(telegramId);
    }

    @Override
    public Optional<User> getUser(Long telegramId) {
        return userRepository.findByTelegramId(String.valueOf(telegramId));
    }

}
