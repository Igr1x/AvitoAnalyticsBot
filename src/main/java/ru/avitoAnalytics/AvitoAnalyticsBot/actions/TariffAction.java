package ru.avitoAnalytics.AvitoAnalyticsBot.actions;

import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.BotButtons;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.TelegramChatUtils;

import java.lang.reflect.InvocationTargetException;

public class TariffAction implements Actions<SendPhoto> {
    @Override
    public SendPhoto handleMessage(Update update, Long chatId) throws TelegramApiException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        String callback = update.getCallbackQuery().getData();
        StringBuilder path = new StringBuilder("classpath:" + callback + ".jpeg");
        return TelegramChatUtils.getPhotoMessage(chatId, "Описание тарифа", path.toString(), new InlineKeyboardMarkup(BotButtons.getTariffButtons(callback.substring(callback.length() - 1))));
    }
}
