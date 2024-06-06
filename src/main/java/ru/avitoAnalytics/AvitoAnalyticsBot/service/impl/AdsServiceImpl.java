package ru.avitoAnalytics.AvitoAnalyticsBot.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.Ads;
import ru.avitoAnalytics.AvitoAnalyticsBot.repositories.AdsRepository;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.AdsService;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.AvitoCostService;

import java.math.BigDecimal;
import java.util.List;

@Service
@AllArgsConstructor
public class AdsServiceImpl implements AdsService {
    AdsRepository adsRepository;
    AvitoCostService avitoCostService;

    @Override
    public BigDecimal findCostByAvitoId(Long avitoId) {
        var item = adsRepository.findById(avitoId);
        return item.map(Ads::getCost).orElse(BigDecimal.ZERO);
    }

    @Override
    public Ads save(Ads item) {
        return adsRepository.save(item);
    }

    @Override
    public void save(List<Ads> adsList) {
        adsRepository.save(adsList);
    }
}
