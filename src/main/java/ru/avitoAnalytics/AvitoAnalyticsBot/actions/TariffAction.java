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
public class TariffAction implements Actions<SendPhoto> {
    @Override
    public SendPhoto handleMessage(Update update, Long chatId) {
        String callback = update.getCallbackQuery().getData();
        String filename = "tarriff%s";
        StringBuilder path = new StringBuilder("/" + callback + ".jpeg");
        InputStream is = getClass().getResourceAsStream(path.toString());
        if (is == null) {
            throw new IllegalStateException("Файл не найден");
        }
        InputFile photo = new InputFile().setMedia(is, String.format(filename, callback));
        return TelegramChatUtils.getPhotoMessage(chatId, "Описание тарифа", photo, new InlineKeyboardMarkup(BotButtons.getTariffButtons(callback.substring(callback.length() - 1))));
    }
}
