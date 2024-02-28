package ru.avitoAnalytics.AvitoAnalyticsBot.actions;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.BotButtons;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.TelegramChatUtils;

@Component
public class StartAction implements Actions<SendPhoto> {

    @Override
    public SendPhoto handleMessage(Update update, Long chatId) {
        return TelegramChatUtils.getPhotoMessage(chatId, "", "classpath:start.jpeg", new InlineKeyboardMarkup(BotButtons.getStartButtons()));
    }
}
