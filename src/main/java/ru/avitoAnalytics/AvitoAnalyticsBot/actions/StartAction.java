package ru.avitoAnalytics.AvitoAnalyticsBot.actions;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.BotButtons;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.TelegramChatUtils;

import java.io.InputStream;

@Component
public class StartAction implements Actions<SendPhoto> {

    @Override
    public SendPhoto handleMessage(Update update, Long chatId) {
        InputStream is = getClass().getResourceAsStream("/start.jpeg");
        if (is == null) {
            throw new IllegalStateException("Файл не найден");
        }
        InputFile photo = new InputFile().setMedia(is, "start.jpeg");
        return TelegramChatUtils.getPhotoMessage(chatId, "", photo, new InlineKeyboardMarkup(BotButtons.getStartButtons()));
        //return TelegramChatUtils.getPhotoMessage(chatId, "", "classpath:start.jpeg", new InlineKeyboardMarkup(BotButtons.getStartButtons()));
    }
}
