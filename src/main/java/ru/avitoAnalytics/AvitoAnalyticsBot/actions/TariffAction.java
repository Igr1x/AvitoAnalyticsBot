package ru.avitoAnalytics.AvitoAnalyticsBot.actions;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.BotButtons;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.TelegramChatUtils;

@Component
public class TariffAction implements Actions<SendPhoto> {
    @Override
    public SendPhoto handleMessage(Update update, Long chatId) {
        String callback = update.getCallbackQuery().getData();
        StringBuilder path = new StringBuilder("classpath:" + callback + ".jpeg");
        return TelegramChatUtils.getPhotoMessage(chatId, "Описание тарифа", path.toString(), new InlineKeyboardMarkup(BotButtons.getTariffButtons(callback.substring(callback.length() - 1))));
    }
}
