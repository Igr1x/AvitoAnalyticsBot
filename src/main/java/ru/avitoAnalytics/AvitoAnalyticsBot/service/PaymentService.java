package ru.avitoAnalytics.AvitoAnalyticsBot.service;

import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery;
import org.telegram.telegrambots.meta.api.objects.payments.PreCheckoutQuery;

public interface PaymentService {
    public AnswerPreCheckoutQuery processPayment(PreCheckoutQuery query);
}
