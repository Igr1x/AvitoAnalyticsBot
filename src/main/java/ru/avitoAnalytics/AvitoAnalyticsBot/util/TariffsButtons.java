package ru.avitoAnalytics.AvitoAnalyticsBot.util;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum TariffsButtons implements ButtonsInfo {
    TARIFF_1("Тариф 1", "tariff1"),
    TARIFF_2("Тариф 2", "tariff2"),
    TARIFF_3("Тариф 3", "tariff3"),
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
