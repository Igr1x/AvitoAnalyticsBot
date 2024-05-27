package ru.avitoAnalytics.AvitoAnalyticsBot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AvitoCost;
import ru.avitoAnalytics.AvitoAnalyticsBot.repositories.AvitoCostJdbcRepository;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.AvitoCostService;

@Service
@RequiredArgsConstructor
public class AvitoCostServiceImpl implements AvitoCostService {
    private final AvitoCostJdbcRepository avitoCostJdbcRepository;

    @Override
    public AvitoCost findAvitoCost(String region, String city, String address, String category, String subcategory, String param) {
        var res = avitoCostJdbcRepository.findAvitoCost(region, city, address, category, subcategory, param);
        return res.orElseGet(() -> {
            return avitoCostJdbcRepository.findAvitoCost("Другой регион", city, address, category, subcategory, param).get();
        });
    }
}
