package ru.avitoAnalytics.AvitoAnalyticsBot.clients.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.avitoAnalytics.AvitoAnalyticsBot.clients.ParserClient;
import ru.avitoAnalytics.AvitoAnalyticsBot.configuration.ParserConfiguration;
import ru.avitoAnalytics.AvitoAnalyticsBot.exceptions.ParserProxyException;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.Product;

import java.util.Map;
import java.util.Objects;

@Component
public class ParserClientImpl implements ParserClient {
    public static final String ENDPOINT_PARSE = "/v1/parser";
    @Qualifier("parserRestTemplate")
    private final RestTemplate restTemplate;
    private final ParserConfiguration configuration;
    private final Gson gson;

    public ParserClientImpl(RestTemplate restTemplate, ParserConfiguration configuration, Gson gson) {
        this.restTemplate = restTemplate;
        this.configuration = configuration;
        this.gson = gson;
    }

    @Override
    @Retryable(retryFor = ParserProxyException.class, maxAttempts = 2, listeners = "parserRetryListener")
    public Product parseAdvertisement(long id) {

        Map<String, String> body = Map.of("proxy", configuration.getProxy(),
                "url", "https://www.avito.ru/" + id);
        try {
            ResponseEntity<String> exchange = restTemplate.exchange(RequestEntity.put(ENDPOINT_PARSE).body(body), String.class);
            JsonObject object = JsonParser.parseString(Objects.requireNonNull(exchange.getBody())).getAsJsonObject();
            String status = object.get("status").getAsJsonPrimitive().getAsString();
            if (status.equals("ok")) {
                return gson.fromJson(object.getAsJsonObject("product").toString(), Product.class);
            }
        } catch (Exception e) {
            if (e instanceof HttpClientErrorException) {
                throw new ParserProxyException(e);
            }
        }
        throw new RuntimeException("Не удалось спарсить объявление с id: " + id);
    }
}
