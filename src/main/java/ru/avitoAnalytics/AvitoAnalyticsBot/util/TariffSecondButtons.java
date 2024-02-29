package ru.avitoAnalytics.AvitoAnalyticsBot.util;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum TariffSecondButtons implements ButtonsInfo{
    CONNECT("Подключить тариф", "connect2"),
    BACK("Назад", "/tariffs");

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
