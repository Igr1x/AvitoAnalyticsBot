package ru.avitoAnalytics.AvitoAnalyticsBot.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.Ads;
import ru.avitoAnalytics.AvitoAnalyticsBot.repositories.AdsRepository;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.AdsService;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class AdsServiceImpl implements AdsService {
    AdsRepository adsRepository;

    @Override
    public BigDecimal findCostByAvitoId(Long avitoId) {
         var item = adsRepository.findById(avitoId);
        return item.map(Ads::getCost).orElse(BigDecimal.ZERO);
    }

    @Override
    public Ads save(Ads item) {
        return adsRepository.save(item);
    }
}
