package ru.avitoAnalytics.AvitoAnalyticsBot.configuration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AstroProxyConfiguration {
    public RestTemplate astroProxyRestTemplate() {
        return new RestTemplateBuilder().build();
    }
}
