package ru.avitoAnalytics.AvitoAnalyticsBot.actions.impl;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.avitoAnalytics.AvitoAnalyticsBot.actions.Actions;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AccountData;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.AccountService;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.ReportHandler;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.TelegramChatUtils;

@Component
public class ReportHandlerAction implements Actions {

    private final AccountService accountService;
    private final ReportHandler reportHandler;

    public ReportHandlerAction(AccountService accountService, ReportHandler reportHandler) {
        this.accountService = accountService;
        this.reportHandler = reportHandler;
    }

    @Override
    public Object handleMessage(Update update, Long chatId) {
        String callbackData = update.getCallbackQuery().getData();
        int indexBeforeId = callbackData.indexOf('-') + 1;
        long accountId = Long.parseLong(callbackData.substring(indexBeforeId));
        try {
            AccountData account = accountService.findById(accountId).orElseThrow(() -> new RuntimeException());
            reportHandler.reportProcess(account);
            return TelegramChatUtils.getMessage(chatId, "Отчёт отправлен на обработку", null);
        } catch (Exception e) {
            System.out.println("пиздец");
        }
        return TelegramChatUtils.getMessage(chatId, "Что то пошло не так!", null);
    }
}
