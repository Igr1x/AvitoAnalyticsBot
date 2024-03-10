package ru.avitoAnalytics.AvitoAnalyticsBot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.Rates;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.User;
import ru.avitoAnalytics.AvitoAnalyticsBot.repositories.RatesRepository;
import ru.avitoAnalytics.AvitoAnalyticsBot.repositories.UserRepository;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.UserService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RatesRepository ratesRepository;

    @Override
    public User saveUser(User user) {
        Rates rates = ratesRepository.findById(4L).get();
        user.setRate(rates);
        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUser() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> getUser(Long telegramId) {
        return userRepository.findByTelegramId(String.valueOf(telegramId));
    }

    @Override
    public void updateUserData(User user) {
        userRepository.save(user);
    }

    @Override
    public void extensionRate(User user) {
        if (!isNotExpiredRate(user)) {
            BigDecimal userBalance = user.getBalance();
            BigDecimal costRate = user.getRate().getCost();
            if (userBalance.compareTo(costRate) >= 0) {
                BigDecimal remains = userBalance.subtract(costRate);
                user.setBalance(remains);
            }
        }
        else {
            Rates withoutRate = ratesRepository.findById(4L).get();
            user.setRate(withoutRate);
        }

        updateUserData(user);
    }

    private boolean isNotExpiredRate(User user) {
        LocalDate endTariff = user.getEndRate();
        LocalDate currentDate = LocalDate.now();
        return currentDate.isBefore(endTariff);
    }

}
