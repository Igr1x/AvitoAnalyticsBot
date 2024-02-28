package ru.avitoAnalytics.AvitoAnalyticsBot.actions;

import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.BotButtons;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.TelegramChatUtils;

import java.lang.reflect.InvocationTargetException;

public class HelpAction implements Actions<SendDocument> {

    @Override
    public SendDocument handleMessage(Update update, Long chatId) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        return TelegramChatUtils.getDocumentMessage(chatId, "Инструкция по пользованию", "classpath:help.pdf", new InlineKeyboardMarkup(BotButtons.getHelpButtons()));
    }
}
