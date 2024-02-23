package ru.avitoAnalytics.AvitoAnalyticsBot.actions;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.UserService;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.BotButtons;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.TelegramChatUtils;

import java.lang.reflect.InvocationTargetException;

public class TariffsAction implements Actions {

    @Override
    public SendPhoto handlePhoto(Update update, Long chatId) throws TelegramApiException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        InlineKeyboardMarkup inlineKeyboard = BotButtons.getHelpButtons();
        return TelegramChatUtils.getPhotoMessage(chatId, "", "classpath:tariffs.jpeg", inlineKeyboard);
    }

    @Override
    public BotApiMethod<?> callback(Update update, Long chatId) {
        return null;
    }
}
