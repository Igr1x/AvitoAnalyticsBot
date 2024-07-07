package ru.avitoAnalytics.AvitoAnalyticsBot.actions.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.avitoAnalytics.AvitoAnalyticsBot.actions.Actions;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AccountData;
import ru.avitoAnalytics.AvitoAnalyticsBot.exceptions.AccountNotFoundException;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.AccountService;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.AdvertisementAggregatorService;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.ReportHandler;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.TelegramChatUtils;

@Component
@Slf4j
@RequiredArgsConstructor
public class CityButtonAction implements Actions<SendMessage> {
    private final AccountService accountService;
    private final ReportHandler reportHandler;
    private final ThreadPoolTaskScheduler taskExecutor;
    private final AdvertisementAggregatorService advertisementAggregatorService;

    @Override
    public SendMessage handleMessage(Update update, Long chatId) {
        String callbackData = update.getCallbackQuery().getData();
        int indexBeforeId = callbackData.indexOf('-') + 1;
        long accountId = Long.parseLong(callbackData.substring(indexBeforeId));
        try {
            AccountData account = accountService.findById(accountId).orElseThrow(() -> new AccountNotFoundException("Account not found"));
            taskExecutor.execute(() -> {
                log.info("Start fill city tab", accountId);
                advertisementAggregatorService.fillingStatisticCities(account);
            });
            return TelegramChatUtils.getMessage(chatId, "Вкладка города скоро будет заполнена!", null);
        } catch (
                AccountNotFoundException e) {
            log.warn(e.getMessage(), e);
        }
        return TelegramChatUtils.getMessage(chatId, "Что-то пошло не так!", null);
    }

}
