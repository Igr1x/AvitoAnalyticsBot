package ru.avitoAnalytics.AvitoAnalyticsBot.util;

import lombok.experimental.UtilityClass;
import org.springframework.util.ResourceUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.FileNotFoundException;

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

    public static SendPhoto getPhotoMessage(long chatId, String caption, String path, InlineKeyboardMarkup inlineKeyboard) throws TelegramApiException {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setPhoto(getFile(path));
        sendPhoto.setCaption(caption);
        if (inlineKeyboard != null) {
            sendPhoto.setReplyMarkup(inlineKeyboard);
        }
        return sendPhoto;
    }

    public static SendDocument getDocumentMessage(long chatId, String caption, String path, InlineKeyboardMarkup inlineKeyboard) {
        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(chatId);
        sendDocument.setDocument(getFile(path));
        sendDocument.setCaption(caption);
        if (inlineKeyboard != null) {
            sendDocument.setReplyMarkup(inlineKeyboard);
        }
        return sendDocument;
    }

    private InputFile getFile(String path) {
        try {
            File file = ResourceUtils.getFile(path);
            return new InputFile(file);
        } catch (FileNotFoundException e) {
        }
        return null;
    }
}