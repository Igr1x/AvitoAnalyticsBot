package ru.avitoAnalytics.AvitoAnalyticsBot.util;

import lombok.experimental.UtilityClass;
import org.springframework.util.ResourceUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.interfaces.Validable;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.List;

@UtilityClass
public class TelegramChatUtils {
    public SendMessage createMessage(long chatId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        return sendMessage;
    }

    public static SendPhoto getPhotoMessage(long chatId, String caption, String path, InlineKeyboardMarkup inlineKeyboard) throws TelegramApiException {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setPhoto(getPhoto(path));
        sendPhoto.setCaption(caption);
        if (inlineKeyboard != null) {
            sendPhoto.setReplyMarkup(inlineKeyboard);
        }
        return sendPhoto;
    }

    private InputFile getPhoto(String path) {
        try {
            File imageFile = ResourceUtils.getFile(path);
            return new InputFile(imageFile);
        } catch (FileNotFoundException e) {
        }
        return null;
    }
}