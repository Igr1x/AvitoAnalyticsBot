package ru.avitoAnalytics.AvitoAnalyticsBot.util;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@UtilityClass
public class TelegramChatUtils {
    public SendMessage getMessage(long chatId, String text, InlineKeyboardMarkup inlineKeyboard) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        if (inlineKeyboard != null) {
            sendMessage.setReplyMarkup(inlineKeyboard);
        }
        return sendMessage;
    }

    public static SendPhoto getPhotoMessage(long chatId, String caption, InputFile photo, InlineKeyboardMarkup inlineKeyboard) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setPhoto(photo);
        sendPhoto.setCaption(caption);
        if (inlineKeyboard != null) {
            sendPhoto.setReplyMarkup(inlineKeyboard);
        }
        return sendPhoto;
    }

    public static SendDocument getDocumentMessage(long chatId, String caption, InputFile file, InlineKeyboardMarkup inlineKeyboard) {
        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(chatId);
        sendDocument.setDocument(file);
        sendDocument.setCaption(caption);
        if (inlineKeyboard != null) {
            sendDocument.setReplyMarkup(inlineKeyboard);
        }
        return sendDocument;
    }
}