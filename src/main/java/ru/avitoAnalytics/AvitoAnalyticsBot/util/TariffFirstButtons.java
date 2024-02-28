package ru.avitoAnalytics.AvitoAnalyticsBot.util;

import lombok.AllArgsConstructor;

@AllArgsConstructor
enum TariffFirstButtons implements ButtonsInfo {
    CONNECT("Подключить тариф", "connect1"),
    BACK("Назад", "/tariffs");

    private final String text;
    private String callbackData;

    @Override
    public String getText() {
        return text;
    }

    @Override
    public String getCallbackData() {
        return callbackData;
    }

}
