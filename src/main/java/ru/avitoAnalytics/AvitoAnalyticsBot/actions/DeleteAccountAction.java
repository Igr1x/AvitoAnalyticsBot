package ru.avitoAnalytics.AvitoAnalyticsBot.actions;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.AccountService;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.BotButtons;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.TelegramChatUtils;

@Component
public class DeleteAccountAction implements Actions<SendMessage> {
    private final AccountService accountService;

    public DeleteAccountAction(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    @Transactional
    public SendMessage handleMessage(Update update, Long chatId) {
        String callback = update.getCallbackQuery().getData();
        int indexBeforeId = callback.indexOf('-') + 1;
        long accountId = Long.parseLong(callback.substring(indexBeforeId));
        accountService.deleteById(accountId);
        return TelegramChatUtils.getMessage(chatId, "Аккаунт удалён", new InlineKeyboardMarkup(BotButtons.getHelpButtons()));
    }
}
