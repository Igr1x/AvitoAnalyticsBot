package ru.avitoAnalytics.AvitoAnalyticsBot.service;

import ru.avitoAnalytics.AvitoAnalyticsBot.entity.Ads;

import java.math.BigDecimal;

public interface AdsService {
    BigDecimal findCostByAvitoId(Long avitoId);
    Ads save(Ads item);
}
