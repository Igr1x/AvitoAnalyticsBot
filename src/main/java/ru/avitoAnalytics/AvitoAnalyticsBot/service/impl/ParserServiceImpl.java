package ru.avitoAnalytics.AvitoAnalyticsBot.service.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.avitoAnalytics.AvitoAnalyticsBot.clients.ParserClient;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.Product;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.ParserService;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ParserServiceImpl implements ParserService {
    private final ParserClient parserClient;

    private Logger log = LoggerFactory.getLogger(ParserService.class);

    @Override
    public Product parseProduct(long id) {
        return parserClient.parseAdvertisement(id);
    }

    @Override
    public List<Product> parseProducts(List<Long> ids) {
        return Collections.emptyList();
    }
}
