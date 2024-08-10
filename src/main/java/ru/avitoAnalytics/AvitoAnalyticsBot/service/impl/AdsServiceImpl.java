package ru.avitoAnalytics.AvitoAnalyticsBot.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AccountData;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.Ads;
import ru.avitoAnalytics.AvitoAnalyticsBot.repositories.AdsRepository;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.AdsService;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.AvitoCostService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AdsServiceImpl implements AdsService {
    AdsRepository adsRepository;
    AvitoCostService avitoCostService;

    @Override
    public Optional<BigDecimal> findCostByAvitoId(Long avitoId) {
        var item = adsRepository.findByAvitoId(avitoId);
        return item.map(Ads::getCost);
    }

    @Override
    public void save(Ads item) {
        adsRepository.save(item);
    }

    @Override
    public void save(List<Ads> adsList) {
        adsRepository.save(adsList);
    }

    @Override
    public Optional<BigDecimal> findAvgCostAdsByAccountIdAndDate(AccountData ownerId, LocalDate date) {
        return adsRepository.findAvgCostAdsByAccountIdAndDate(ownerId, date);
    }

    @Override
    public Optional<BigDecimal> findAvgCostAdsByAccountId(AccountData ownerId) {
        return adsRepository.findAvgCostAdsByAccountId(ownerId);
    }

    @Override
    public List<Ads> findAllAdsByAccountIdAndDate(AccountData ownerId, LocalDate date) {
        return adsRepository.findAllAdsByAccountIdAndDate(ownerId, date);
    }

    @Override
    public List<Ads> findAdsFilter(AccountData ownerId, LocalDate pubDate) {
        return adsRepository.findAdsFilter(ownerId, pubDate);
    }

    @Override
    public Optional<Ads> findByAvitoId(Long avitoId) {
        return adsRepository.findByAvitoId(avitoId);
    }
}
