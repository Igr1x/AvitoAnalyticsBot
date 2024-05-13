package ru.avitoAnalytics.AvitoAnalyticsBot.actions;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.Rates;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.User;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.RatesService;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.UserService;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.BotButtons;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.TelegramChatUtils;

import java.io.InputStream;

@Component
public class BalanceAction implements Actions<SendPhoto> {
    private final RatesService ratesService;
    private final UserService userService;

    public BalanceAction(RatesService ratesService, UserService userService) {
        this.ratesService = ratesService;
        this.userService = userService;
    }

    @Override
    public SendPhoto handleMessage(Update update, Long chatId) {
        User user = userService.getUser(chatId).orElseThrow();
        Rates rate = ratesService.getRate(user.getRate().getId()).orElseThrow();
        String text = "Баланс: %.2f руб.\nПодключенный тариф: %s";
        InputStream is = getClass().getResourceAsStream("/balance.jpeg");
        if (is == null) {
            throw new IllegalStateException("Файл не найден");
        }
        InputFile photo = new InputFile().setMedia(is, "balance.jpeg");
        return TelegramChatUtils.getPhotoMessage(chatId, String.format(text, user.getBalance(), rate.getTitle()), photo, new InlineKeyboardMarkup(BotButtons.getBalanceButtons()));
    }
}
