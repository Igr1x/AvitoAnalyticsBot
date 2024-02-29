package ru.avitoAnalytics.AvitoAnalyticsBot.actions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AccountData;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.User;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.AccountService;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.GoogleSheetsService;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.UserService;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.BotButtons;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.TelegramChatUtils;

@Component
@Slf4j
@RequiredArgsConstructor
public class AddAccountAction implements Actions<SendMessage> {

    private final GoogleSheetsService googleSheetsService;
    private final UserService userService;
    private final AccountService accountService;

    @Override
    public SendMessage handleMessage(Update update, Long chatId) {
        String text = "Введите следующие данные об аккаунте:\n" +
                "Название аккаунта\n" +
                "user id\n" +
                "client id\n" +
                "client secret\n" +
                "ссылку на таблицу";
        return new SendMessage(chatId.toString(), text);
    }

    @Override
    public BotApiMethod<?> callback(Update update, Long chatId) {
        Message msg = update.getMessage();
        String[] requestsUser = msg.getText().split("\n");
        String accountName = requestsUser[0];
        Long userId = Long.parseLong(requestsUser[1]);
        String clientId = requestsUser[2];
        String clientSecret = requestsUser[3];
        String sheetsRef = requestsUser[4];
        if (googleSheetsService.checkExistSheets(sheetsRef)) {
            User user = userService.getUser(chatId).orElseThrow();
            AccountData accountData = new AccountData(user, userId, clientId, clientSecret, sheetsRef, accountName);
            accountService.saveAccount(accountData);
            String text = "Ваш аккаунт успешно добавлен";
            log.info("Account was added successfully for client: " + clientId);
            return TelegramChatUtils.getMessage(chatId, text, new InlineKeyboardMarkup(BotButtons.getHelpButtons()));

        }
        String text = "Таблицы по данной ссылке не существует либо вы не предоставили открытый доступ!";
        return TelegramChatUtils.getMessage(chatId, text, new InlineKeyboardMarkup(BotButtons.getHelpButtons()));
    }

}
