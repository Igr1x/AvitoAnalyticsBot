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
    public void saveAccount(AccountData accountData) {
        accountRepository.save(accountData);
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
    public Optional<AccountData> findById(Long id) {
        return accountRepository.findById(id);
    }

    @Override
    public List<AccountData> findAllBySheetsRef(String sheetsRef) {
        return accountRepository.findAllBySheetsRef(sheetsRef);
    }

    @Override
    public Optional<AccountData> findByAccountName(String accountName) {
        return accountRepository.findByAccountName(accountName);
    }

    @Override
    public List<AccountData> findAll() {
        return accountRepository.findAll();
    }

    @Override
    public List<AccountData> findByUserOwnerId(Long userOwnerId) {
        return accountRepository.findByUserOwnerId(userOwnerId);
    }

    @Override
    public List<String> findUniqueSheetsRef() {
        return accountRepository.findUniqueSheetsRef();
    }

    @Override
    public Optional<AccountData> findByUserId(Long userId) {
        return accountRepository.findByUserId(userId);
    }
}
