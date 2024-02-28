package ru.avitoAnalytics.AvitoAnalyticsBot.util;

import lombok.AllArgsConstructor;

@AllArgsConstructor
enum TariffSecondButtons implements ButtonsInfo {
    CONNECT("Подключить тариф", "connect2"),
    BACK("Назад", "backToTariffs");

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
