package ru.avitoAnalytics.AvitoAnalyticsBot.actions;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.avitoAnalytics.AvitoAnalyticsBot.repositories.AccountRepository;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.BotButtons;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.TelegramChatUtils;

@Component
public class DeleteAccountAction implements Actions<SendMessage> {
    private final AccountRepository accountRepository;

    public DeleteAccountAction(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public SendMessage handleMessage(Update update, Long chatId) {
        String callback = update.getCallbackQuery().getData();
        int indexBeforeId = callback.indexOf('-') + 1;
        long accountId = Long.parseLong(callback.substring(indexBeforeId));
        accountRepository.deleteById(accountId);
        return TelegramChatUtils.getMessage(chatId, "Аккаунт удалён", new InlineKeyboardMarkup(BotButtons.getHelpButtons()));
    }
}
