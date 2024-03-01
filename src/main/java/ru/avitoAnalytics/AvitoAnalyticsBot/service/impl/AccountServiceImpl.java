package ru.avitoAnalytics.AvitoAnalyticsBot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AccountData;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.User;
import ru.avitoAnalytics.AvitoAnalyticsBot.repositories.AccountRepository;
import ru.avitoAnalytics.AvitoAnalyticsBot.repositories.UserRepository;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.AccountService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Override
    public Long saveAccount(AccountData accountData) {
        return accountRepository.save(accountData).getId();
    }

    @Override
    public Optional<User> getUser(Long telegramId) {
        return userRepository.findByTelegramId(String.valueOf(telegramId));
    }

    @Override
    @Transactional
    public void deleteById(long accountId) {
        accountRepository.deleteById(accountId);
    }

    @Override
    public List<AccountData> findByUserId(Long userOwnerId) {
        return accountRepository.findByUserOwnerId(userOwnerId);
    }

    @Override
    public Optional<AccountData> findById(Long id) {
        return accountRepository.findById(id);
    }
}
