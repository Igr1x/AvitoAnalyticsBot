package ru.avitoAnalytics.AvitoAnalyticsBot.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.Rates;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.User;
import ru.avitoAnalytics.AvitoAnalyticsBot.repositories.UserRepository;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.RatesService;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.UserService;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.BotButtons;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.TelegramChatUtils;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;

@Component
public class ConnectTariff implements Actions<SendMessage> {
    @Autowired
    private RatesService ratesService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @Override
    public SendMessage handleMessage(Update update, Long chatId) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        String callbackData = update.getCallbackQuery().getData();
        String rateId = callbackData.substring(callbackData.length() - 1);
        Rates rate = ratesService.getRate(Long.valueOf(rateId)).orElseThrow();
        User user = userService.getUser(chatId).orElseThrow();
        BigDecimal balance = user.getBalance();
        if (balance.compareTo(rate.getCost()) < 0) {
            return TelegramChatUtils.getMessage(chatId, "На балансе недостаточно средств!", BotButtons.getBalanceButtons());
        }
        user.setBalance(balance.subtract(rate.getCost()));
        user.setRate(rate);
        userRepository.save(user);
        return TelegramChatUtils.getMessage(chatId, "Тариф подключен!", BotButtons.getHelpButtons());
    }
}
