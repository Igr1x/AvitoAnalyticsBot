package ru.avitoAnalytics.AvitoAnalyticsBot.actions;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.lang.reflect.InvocationTargetException;


public interface Actions {

    default SendMessage handleMessage(Update update, Long chatId) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        return null;
    }

    default SendPhoto handlePhoto(Update update, Long chatId) throws TelegramApiException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return null;
    }

    default SendMediaGroup handleMediaGroup(Update update, Long chatId){
        return null;
    }

    default BotApiMethod<?> callback(Update update, Long chatId) {
        return null;
    }
}
