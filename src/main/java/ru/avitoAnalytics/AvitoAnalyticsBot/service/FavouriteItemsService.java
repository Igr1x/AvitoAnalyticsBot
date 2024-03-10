package ru.avitoAnalytics.AvitoAnalyticsBot.service;

import ru.avitoAnalytics.AvitoAnalyticsBot.entity.FavouriteItems;

import java.math.BigDecimal;

public interface FavouriteItemsService {
    BigDecimal findCostById(Long id);
    FavouriteItems save(FavouriteItems item);
}
