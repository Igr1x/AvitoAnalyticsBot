package ru.avitoAnalytics.AvitoAnalyticsBot.util;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@UtilityClass
public class TelegramChatUtils {

    public void sendMessage(TelegramLongPollingBot bot, long chatId, String text) throws TelegramApiException {
        SendMessage sendMessage = createMessage(chatId, text);
        bot.execute(sendMessage);
    }

    public void sendKeyboardMessage(TelegramLongPollingBot bot, long chatId, String text, InlineKeyboardMarkup markup) throws TelegramApiException {
        SendMessage sendMessage = createMessage(chatId, text);
        sendMessage.setReplyMarkup(markup);
        bot.execute(sendMessage);
    }

    private SendMessage createMessage(long chatId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        return sendMessage;
    }
}