package ru.avitoAnalytics.AvitoAnalyticsBot.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.Rates;
import ru.avitoAnalytics.AvitoAnalyticsBot.repositories.RatesRepository;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.RatesService;

import java.util.Optional;

@Service
public class RatesServiceImpl implements RatesService {
    private final RatesRepository ratesRepository;

    @Autowired
    public RatesServiceImpl(RatesRepository ratesRepository) {
        this.ratesRepository = ratesRepository;
    }

    @Override
    public Optional<Rates> getRate(Long id) {
        return ratesRepository.findById(id);
    }
}
