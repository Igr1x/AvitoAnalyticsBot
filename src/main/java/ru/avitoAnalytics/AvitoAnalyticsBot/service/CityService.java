package ru.avitoAnalytics.AvitoAnalyticsBot.service;


import ru.avitoAnalytics.AvitoAnalyticsBot.entity.City;

import java.util.Optional;

public interface CityService {
    Optional<City> getCityByName(String cityName);
}
