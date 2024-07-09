package ru.avitoAnalytics.AvitoAnalyticsBot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.City;
import ru.avitoAnalytics.AvitoAnalyticsBot.repositories.CityRepository;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.CityService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CityServiceImpl implements CityService {
    private final CityRepository cityRepository;

    @Override
    public Optional<City> getCityByName(String cityName) {
        return cityRepository.findByCityName(cityName);
    }
}
