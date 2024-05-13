package ru.avitoAnalytics.AvitoAnalyticsBot.actions;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.BotButtons;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.TelegramChatUtils;

import java.io.InputStream;

@Component
public class HelpAction implements Actions<SendDocument> {

    @Override
    public SendDocument handleMessage(Update update, Long chatId) {
        InputStream is = getClass().getResourceAsStream("/help.pdf");
        if (is == null) {
            throw new IllegalStateException("Файл не найден");
        }
        InputFile pdf = new InputFile().setMedia(is, "help.jpeg");
        return TelegramChatUtils.getDocumentMessage(chatId, "Инструкция по пользованию", pdf, new InlineKeyboardMarkup(BotButtons.getHelpButtons()));
    }
}
