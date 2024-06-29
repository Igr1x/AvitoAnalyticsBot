package ru.avitoAnalytics.AvitoAnalyticsBot.actions.impl;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.avitoAnalytics.AvitoAnalyticsBot.actions.Actions;
import ru.avitoAnalytics.AvitoAnalyticsBot.configuration.BotConfiguration;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AccountData;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.AccountService;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.BotButtons;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.TelegramChatUtils;

@Component
public class SelectAccountAction implements Actions<SendMessage> {
    final AccountService accountService;

    public SelectAccountAction(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public SendMessage handleMessage(Update update, Long chatId) {
        String callbackData = update.getCallbackQuery().getData();
        int indexBeforeId = callbackData.indexOf('-') + 1;
        long accountId = Long.parseLong(callbackData.substring(indexBeforeId));
        AccountData account = accountService.findById(accountId).orElseThrow();
        var keyboard = BotButtons.getSelectAccountButtons(chatId, accountId);
        if (!account.isReport()) {
            keyboard.add(0, BotButtons.getReportButton(accountId));
        }
        return TelegramChatUtils.getMessage(chatId, account.toString(), new InlineKeyboardMarkup(keyboard));
    }
}
