package ru.avitoAnalytics.AvitoAnalyticsBot.actions;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AccountData;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.User;
import ru.avitoAnalytics.AvitoAnalyticsBot.repositories.AccountRepository;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.UserService;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.BotButtons;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.TelegramChatUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class AccountsAction implements Actions<SendMessage>{
    private final AccountRepository accountRepository;
    private final UserService userService;

    public AccountsAction(AccountRepository accountRepository, UserService userService) {
        this.accountRepository = accountRepository;
        this.userService = userService;
    }

    @Override
    public SendMessage handleMessage(Update update, Long chatId) {
        User user = userService.getUser(chatId).orElseThrow();
        List<AccountData> accounts = accountRepository.findByUserId(user.getId());
        StringBuilder text = new StringBuilder();
        List<Long> accountsId = new ArrayList<>();
        for (int i = 0; i < accounts.size(); i++) {
            text.append("Аккаунт №").append(i + 1).append('\n');
            text.append("Client_id - ").append(accounts.get(i).getClientId()).append('\n');
            text.append("Client_secret - ").append(accounts.get(i).getClientSecret()).append("\n\n");
            accountsId.add(accounts.get(i).getId());
        }

        return TelegramChatUtils.getMessage(chatId, text.toString(), new InlineKeyboardMarkup(BotButtons.getAccountsButtons(accounts.size(), accountsId)));
    }
}
