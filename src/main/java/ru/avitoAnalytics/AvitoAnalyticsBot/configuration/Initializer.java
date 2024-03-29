package ru.avitoAnalytics.AvitoAnalyticsBot.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.avitoAnalytics.AvitoAnalyticsBot.controller.TelegramBot;

@Component
@Slf4j
@RequiredArgsConstructor
public class Initializer {

    private final TelegramBot bot;

    @EventListener({ContextRefreshedEvent.class})
    public void init() {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(bot);
            log.info("Bot {} was registered!", bot.getBotUsername());
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

}
