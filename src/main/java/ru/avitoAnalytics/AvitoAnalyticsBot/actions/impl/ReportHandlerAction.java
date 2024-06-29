package ru.avitoAnalytics.AvitoAnalyticsBot.actions.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.avitoAnalytics.AvitoAnalyticsBot.actions.Actions;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AccountData;
import ru.avitoAnalytics.AvitoAnalyticsBot.exceptions.AccountNotFoundException;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.AccountService;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.ReportHandler;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.TelegramChatUtils;

@Component
@Slf4j
public class ReportHandlerAction implements Actions {

    private final AccountService accountService;
    private final ReportHandler reportHandler;
    private final ThreadPoolTaskScheduler taskExecutor;

    public ReportHandlerAction(AccountService accountService, ReportHandler reportHandler, ThreadPoolTaskScheduler taskExecutor) {
        this.accountService = accountService;
        this.reportHandler = reportHandler;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public Object handleMessage(Update update, Long chatId) {
        String callbackData = update.getCallbackQuery().getData();
        int indexBeforeId = callbackData.indexOf('-') + 1;
        long accountId = Long.parseLong(callbackData.substring(indexBeforeId));
        try {
            AccountData account = accountService.findById(accountId).orElseThrow(() -> new AccountNotFoundException("Account not found"));
            if (!account.isReport()) {
                taskExecutor.execute(() -> {
                    log.info("Start processing report for account {}", accountId);
                    reportHandler.reportProcess(account);
                });
                return TelegramChatUtils.getMessage(chatId, "Отчёт отправлен на обработку", null);
            }
            return TelegramChatUtils.getMessage(chatId, "Отчёт по данному аккаунту уже в обработке!", null);
        } catch (AccountNotFoundException e) {
            log.warn(e.getMessage(), e);
        }
        return TelegramChatUtils.getMessage(chatId, "Что-то пошло не так!", null);
    }
}
