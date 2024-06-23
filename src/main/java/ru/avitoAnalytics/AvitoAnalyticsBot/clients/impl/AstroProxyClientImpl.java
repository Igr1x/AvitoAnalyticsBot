package ru.avitoAnalytics.AvitoAnalyticsBot.clients.impl;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.avitoAnalytics.AvitoAnalyticsBot.clients.AstroProxyClient;
import ru.avitoAnalytics.AvitoAnalyticsBot.exceptions.ParserProxyException;

@Component
public class AstroProxyClientImpl implements AstroProxyClient {

    @Qualifier("astroProxyRestTemplate")
    private final RestTemplate astroProxyRestTemplate;

    public AstroProxyClientImpl(RestTemplate astroProxyRestTemplate) {
        this.astroProxyRestTemplate = astroProxyRestTemplate;
    }

    @Override
    @SneakyThrows
    @Retryable(retryFor = {HttpClientErrorException.class},
            backoff = @Backoff(delay = 30000))
    public void changeProxy() {
        astroProxyRestTemplate.getForObject("https://astroproxy.com/api/v1/ports/5330664/newip?token=1c6bc94ce2385590&id=5330664", String.class);
    }
}
