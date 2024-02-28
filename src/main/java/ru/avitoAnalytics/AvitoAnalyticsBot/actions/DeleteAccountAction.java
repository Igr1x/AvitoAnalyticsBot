package ru.avitoAnalytics.AvitoAnalyticsBot.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.avitoAnalytics.AvitoAnalyticsBot.repositories.AccountRepository;
import ru.avitoAnalytics.AvitoAnalyticsBot.repositories.UserRepository;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.BotButtons;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.TelegramChatUtils;

import java.lang.reflect.InvocationTargetException;

@Component
public class DeleteAccountAction implements Actions<SendMessage> {
    @Autowired
    AccountRepository accountRepository;

    @Override
    public SendMessage handleMessage(Update update, Long chatId) throws TelegramApiException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        String callback = update.getCallbackQuery().getData();
        int indexBeforeId = callback.indexOf('-') + 1;
        long accountId = Long.parseLong(callback.substring(indexBeforeId));
        accountRepository.deleteById(accountId);
        return TelegramChatUtils.getMessage(chatId, "Аккаунт удалён", new InlineKeyboardMarkup(BotButtons.getHelpButtons()));
    }
}
