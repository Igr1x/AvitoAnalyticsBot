package ru.avitoAnalytics.AvitoAnalyticsBot.util;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum TariffThirdButtons implements ButtonsInfo{
    CONNECT("Подключить тариф", "connect3"),
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

