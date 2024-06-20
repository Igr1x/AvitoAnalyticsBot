package ru.avitoAnalytics.AvitoAnalyticsBot.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.Rates;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.User;
import ru.avitoAnalytics.AvitoAnalyticsBot.repositories.UserRepository;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.RatesService;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.UserService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RatesService ratesService;

    @Override
    public void saveUser(User user) {
        Rates rates = getRates(4L);
        user.setRate(rates);
        userRepository.save(user);
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
                LocalDate endRate = user.getEndRate().plusMonths(1);
                user.setBalance(remains);
                user.setEndRate(endRate);
            }
        }
        else {
            Rates withoutRate = getRates(4L);
            user.setRate(withoutRate);
        }
        updateUserData(user);
    }

    private boolean isNotExpiredRate(User user) {
        LocalDate endTariff = user.getEndRate();
        LocalDate currentDate = LocalDate.now();
        return currentDate.isBefore(endTariff);
    }

    private Rates getRates(Long id) {
        return ratesService.getRate(id).orElseThrow(() -> {
            log.error("Rates {} not found", id);
            return new RuntimeException();
        });
    }
}
