package ru.avitoAnalytics.AvitoAnalyticsBot.service;


import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AvitoCost;

public interface AvitoCostService {
    AvitoCost findAvitoCost(String region, String city, String address,
                            String category, String subcategory, String param);
}
