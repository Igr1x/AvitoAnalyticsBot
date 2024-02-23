package ru.avitoAnalytics.AvitoAnalyticsBot.util;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum StartButtons implements ButtonsInfo{
    HELP("Помощь", "/help"),
    TARIFFS("Тарифы", "/tariffs"),
    BALANCE("Баланс", "/balance"),
    ADD_ACCOUNT("Подключить аккаунт", "/add"),
    ALL_ACCOUNTS("Подключенные аккаунты", "/accounts");

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
