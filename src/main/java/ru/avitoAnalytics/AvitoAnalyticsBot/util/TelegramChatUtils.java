package ru.avitoAnalytics.AvitoAnalyticsBot.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AccountData;
import ru.avitoAnalytics.AvitoAnalyticsBot.enums.SurveyStatus;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.AccountService;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.GoogleSheetsService;

@UtilityClass
@Slf4j
public class TelegramChatUtils {

    public void sendMessage(TelegramLongPollingBot bot, long chatId, String text) {
        try {
            SendMessage sendMessage = createMessage(chatId, text);
            bot.execute(sendMessage);
        } catch (TelegramApiException ex) {
            log.error("TelegramApiException occurred");
        }
    }

    public SurveyStatus processingMessage(TelegramLongPollingBot bot, Update update,
                                  AccountData accountData, SurveyStatus surveyStatus,
                                  GoogleSheetsService googleSheetsService) throws TelegramApiException {
        String responseUser = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        switch (surveyStatus) {
            case CLIENT_ID -> {
                accountData.setClientId(responseUser);
                sendMessage(bot, chatId, "Введите свой client_secret:");
                return SurveyStatus.CLIENT_SECRET;
            }
            case CLIENT_SECRET -> {
                accountData.setClientSecret(responseUser);
                sendMessage(bot, chatId, "Введите свой ссылку на свою Google Таблицу:");
                return SurveyStatus.SHEETS_REF;
            }
            case SHEETS_REF -> {
                if (googleSheetsService.checkExistSheets(responseUser)) {
                    accountData.setSheetsRef(responseUser);
                    return SurveyStatus.END;
                }
                else {
                    final String messageRepeat = "Таблицы по данной ссылке не существует либо вы не предоставили открытый доступ!\n" +
                            "Введите верную ссылку на таблицу или сделайте её доступ открытой.";
                    sendMessage(bot, chatId, messageRepeat);
                }
            }
        }
        return surveyStatus;
    }

    private SendMessage createMessage(long chatId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        return sendMessage;
    }

}