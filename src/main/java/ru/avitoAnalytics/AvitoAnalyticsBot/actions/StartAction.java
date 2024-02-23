package ru.avitoAnalytics.AvitoAnalyticsBot.actions;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.BotButtons;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.TelegramChatUtils;

import java.lang.reflect.InvocationTargetException;

public class StartAction implements Actions {

    @Override
    public SendPhoto handlePhoto(Update update, Long chatId) throws TelegramApiException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        InlineKeyboardMarkup in = BotButtons.getStartButtons();
        return TelegramChatUtils.getPhotoMessage(chatId, "Привет!", "classpath:start.jpeg", in);
    }

    @Override
    public BotApiMethod callback(Update update, Long chatId) {
        return null;
    }
}
