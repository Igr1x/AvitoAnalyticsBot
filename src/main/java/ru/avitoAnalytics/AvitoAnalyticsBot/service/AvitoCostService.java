package ru.avitoAnalytics.AvitoAnalyticsBot.service;


import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AvitoCost;

import java.util.Optional;

public interface AvitoCostService {
    Optional<AvitoCost> findAvitoCost(String region, String city, String address,
                                      String category, String subcategory, String param);
}
