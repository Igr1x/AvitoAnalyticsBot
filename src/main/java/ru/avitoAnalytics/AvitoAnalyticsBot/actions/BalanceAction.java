package ru.avitoAnalytics.AvitoAnalyticsBot.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.Rates;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.User;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.RatesService;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.UserService;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.BotButtons;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.TelegramChatUtils;

import java.lang.reflect.InvocationTargetException;

@Component
public class BalanceAction implements Actions<SendPhoto> {
    @Autowired
    private RatesService ratesService;
    @Autowired
    private UserService userService;

    @Override
    public SendPhoto handleMessage(Update update, Long chatId) throws TelegramApiException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        User user = userService.getUser(chatId).orElseThrow();
        Rates rate = ratesService.getRate(user.getRate().getId()).orElseThrow();
        String text = "Баланс: %.2f руб.\nПодключенный тариф: %s";
        return TelegramChatUtils.getPhotoMessage(chatId, String.format(text, user.getBalance(), rate.getTitle()), "classpath:balance.jpeg", BotButtons.getBalanceButtons());
    }
}
