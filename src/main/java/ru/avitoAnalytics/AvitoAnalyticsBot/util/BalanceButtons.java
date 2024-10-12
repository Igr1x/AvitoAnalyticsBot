package ru.avitoAnalytics.AvitoAnalyticsBot.util;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum BalanceButtons implements ButtonsInfo{
    PAY("Пополнить баланс", "/payment"),
    BACK_MENU("Вернуться в главное меню", "/start");

    private final String text;
    private final String callbackData;

    @Override
    public String getText() {
        return text;
    }

    @Override
    public String getCallbackData() {
        return callbackData;
    }
}
