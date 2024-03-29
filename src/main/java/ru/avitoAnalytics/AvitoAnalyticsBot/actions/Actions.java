package ru.avitoAnalytics.AvitoAnalyticsBot.actions;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface Actions<T> {

    default T handleMessage(Update update, Long chatId) {
        return null;
    }

    default BotApiMethod<?> callback(Update update, Long chatId) {
        return null;
    }
}
