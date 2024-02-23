package ru.avitoAnalytics.AvitoAnalyticsBot.actions;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.BotButtons;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.TelegramChatUtils;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class HelpAction implements Actions {

    @Override
    public SendMessage handleMessage(Update update, Long chatId) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        SendMessage sendMessage = TelegramChatUtils.createMessage(chatId, "Вкладка помощь");
        sendMessage.setReplyMarkup(BotButtons.getHelpButtons());
        return sendMessage;
    }

    @Override
    public SendMediaGroup handleMediaGroup(Update update, Long chatId) {

        List<InputMedia> photo = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            InputStream inputStream = HelpAction.class.getResourceAsStream("/help" + i + ".jpg");
            if (inputStream != null) {
                var file = new InputMediaPhoto();
                file.setMedia(inputStream, "photo"+i);
                photo.add(file);
            }
        }
        SendMediaGroup send = new SendMediaGroup();
        send.setChatId(chatId);
        send.setMedias(photo);
        return send;
    }

    /*@Override
    public SendPhoto handlePhoto(Update update, Long chatId) throws TelegramApiException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        return TelegramChatUtils.getPhotoMessage(chatId, "", "classpath:help1.jpeg", BotButtons.getHelpButtons());
    }*/

    @Override
    public BotApiMethod<?> callback(Update update, Long chatId) {
        return null;
    }
}
