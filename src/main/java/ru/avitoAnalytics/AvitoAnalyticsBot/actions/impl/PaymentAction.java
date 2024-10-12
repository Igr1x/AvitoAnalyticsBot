package ru.avitoAnalytics.AvitoAnalyticsBot.actions.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.invoices.SendInvoice;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice;
import ru.avitoAnalytics.AvitoAnalyticsBot.actions.Actions;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.avitoAnalytics.AvitoAnalyticsBot.configuration.BotConfiguration;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.UserService;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.TelegramChatUtils;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentAction implements Actions<SendMessage> {

    private final BotConfiguration botConfiguration;

    @Override
    public SendMessage handleMessage(Update update, Long chatId) {
        var msg = "Введите сумму пополнения:\nМинимальная сумма пополнения: 150 руб.";
        return new SendMessage(chatId.toString(), msg);
    }

    @Override
    public BotApiMethod<?> callback(Update update, Long chatId) {
        try {
            int sum = Integer.parseInt(update.getMessage().getText().trim());
            var price = LabeledPrice.builder()
                    .label("RUB")
                    .amount(sum * 100)
                    .build();

            return SendInvoice.builder()
                    .chatId(chatId)
                    .currency("RUB")
                    .providerToken(botConfiguration.getPaymentToken())
                    .title("Перевод денежных средств")
                    .description("Пополнение баланса бота")
                    .payload("test")
                    .price(price)
                    .startParameter("test")
                    .build();
        } catch (NumberFormatException e) {
            String errMsg = "Введите корректную сумму пополнения!";
            return TelegramChatUtils.getMessage(chatId, errMsg, null);
        }
    }
}
