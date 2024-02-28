package ru.avitoAnalytics.AvitoAnalyticsBot.actions;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaBotMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.lang.reflect.InvocationTargetException;


public interface Actions<T> {

    default T handleMessage(Update update, Long chatId) throws TelegramApiException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return null;
    }

    default BotApiMethod<?> callback(Update update, Long chatId) {
        return null;
    }
}
