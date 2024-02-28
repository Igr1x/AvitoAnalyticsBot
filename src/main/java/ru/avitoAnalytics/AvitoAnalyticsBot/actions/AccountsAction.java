package ru.avitoAnalytics.AvitoAnalyticsBot.actions;

import org.springframework.beans.factory.annotation.Autowired;
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

import java.lang.reflect.InvocationTargetException;
import java.util.List;

@Component
public class AccountsAction implements Actions<SendMessage>{
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    UserService userService;

    @Override
    public SendMessage handleMessage(Update update, Long chatId) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        User user = userService.getUser(chatId).orElseThrow();
        List<AccountData> accounts = accountRepository.findByUserId(user.getId());
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < accounts.size(); i++) {
            text.append("Аккаунт №").append(i + 1).append('\n');
            text.append("Client_id - ").append(accounts.get(i).getClientId()).append('\n');
            text.append("Client_secret - ").append(accounts.get(i).getClientSecret()).append("\n\n");
        }

        return TelegramChatUtils.getMessage(chatId, text.toString(), new InlineKeyboardMarkup(BotButtons.getAccountsButtons(accounts.size(), chatId)));
    }
}
