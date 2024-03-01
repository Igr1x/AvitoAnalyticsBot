package ru.avitoAnalytics.AvitoAnalyticsBot.configuration;


import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class AvitoConfiguration {

    @Value("${avito.statistic.max_items_per_request}")
    private int maxItemsPerRequest;

    @Value("${avito.ads.max_items_per_request}")
    private int maxAdsPerRequest;
}
