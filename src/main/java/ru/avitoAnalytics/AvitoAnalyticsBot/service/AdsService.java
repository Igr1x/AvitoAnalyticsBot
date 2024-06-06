package ru.avitoAnalytics.AvitoAnalyticsBot.service;

import ru.avitoAnalytics.AvitoAnalyticsBot.entity.Ads;

import java.math.BigDecimal;
import java.util.List;

public interface AdsService {
    BigDecimal findCostByAvitoId(Long avitoId);
    Ads save(Ads item);
    void save(List<Ads> adsList);
}
