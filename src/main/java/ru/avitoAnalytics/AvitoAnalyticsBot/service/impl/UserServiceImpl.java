package ru.avitoAnalytics.AvitoAnalyticsBot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AccountData;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.Rates;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.User;
import ru.avitoAnalytics.AvitoAnalyticsBot.exceptions.RepeatAccountDataException;
import ru.avitoAnalytics.AvitoAnalyticsBot.repositories.AccountRepository;
import ru.avitoAnalytics.AvitoAnalyticsBot.repositories.RatesRepository;
import ru.avitoAnalytics.AvitoAnalyticsBot.repositories.UserRepository;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    public Optional<User> getUser(Long telegramId) {
        return userRepository.findByTelegramId(String.valueOf(telegramId));
    }

    @Override
    public User addAccount(User user, AccountData accountData) throws RepeatAccountDataException {
        List<AccountData> accountDataList = user.getAccounts();
        if (accountDataList.contains(accountData)) {
            throw new RepeatAccountDataException("У пользователя уже добавлен данный аккаунт");
        }
        accountDataList.add(accountData);
        user.setAccounts(accountDataList);
        return userRepository.save(user);
    }

}
