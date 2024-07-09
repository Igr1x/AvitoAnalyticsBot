package ru.avitoAnalytics.AvitoAnalyticsBot.clients.impl;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import ru.avitoAnalytics.AvitoAnalyticsBot.clients.ParserClient;
import ru.avitoAnalytics.AvitoAnalyticsBot.exceptions.ItemNotFoundException;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.Product;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.CityService;

import java.util.Map;

@Component
public class ParserClientImpl implements ParserClient {
    public static final String ENDPOINT_PARSE = "http://localhost:8000/process_id";
    @Qualifier("parserRestTemplate")
    private final RestTemplate restTemplate;

    public ParserClientImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Product parseAdvertisement(long id) throws ItemNotFoundException {
        Map<String, Object> body = Map.of(
                "id", String.valueOf(id));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);
        try {
            return restTemplate.postForObject(ENDPOINT_PARSE, req, Product.class);
        } catch (HttpStatusCodeException e) {
            throw new ItemNotFoundException();
        }
    }
}
