package ru.avitoAnalytics.AvitoAnalyticsBot.service;

import ru.avitoAnalytics.AvitoAnalyticsBot.entity.Rates;

import java.util.Optional;

public interface RatesService {
    Optional<Rates> getRate(Long id);
}
