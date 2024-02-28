package ru.avitoAnalytics.AvitoAnalyticsBot.actions;

import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.BotButtons;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.TelegramChatUtils;

import java.lang.reflect.InvocationTargetException;

public class StartAction implements Actions<SendPhoto> {

    @Override
    public SendPhoto handleMessage(Update update, Long chatId) throws TelegramApiException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return TelegramChatUtils.getPhotoMessage(chatId, "", "classpath:start.jpeg", new InlineKeyboardMarkup(BotButtons.getStartButtons()));
    }
}
