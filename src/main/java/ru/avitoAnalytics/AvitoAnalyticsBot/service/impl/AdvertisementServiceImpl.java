package ru.avitoAnalytics.AvitoAnalyticsBot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.avitoAnalytics.AvitoAnalyticsBot.configuration.AvitoConfiguration;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AccountData;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.Advertisement;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.ListAdvertisement;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.AdvertisementService;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.StatisticAvitoService;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AdvertisementServiceImpl implements AdvertisementService {

    private static final String URL = "https://api.avito.ru/core/v1/items?page=%s&per_page=%s";

    private final AvitoConfiguration avitoConfiguration;

    @Override
    public List<Advertisement> getAllAdvertisements(String token) {
        int page = 1;
        List<Advertisement> result = new ArrayList<>();
        List<Advertisement> advertisementsPage = getAdvertisementsResponse(token,
                avitoConfiguration.getMaxAdsPerRequest(),
                page++);
        while (!advertisementsPage.isEmpty()) {
            result.addAll(advertisementsPage);
            advertisementsPage = getAdvertisementsResponse(token,
                    avitoConfiguration.getMaxAdsPerRequest(),
                    page++);
        }
        return result;
    }

    private List<Advertisement> getAdvertisementsResponse(String token, Integer perPage, Integer page) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        String currentUrl = String.format(URL, page, perPage);
        var response = restTemplate.exchange(currentUrl, HttpMethod.GET, new HttpEntity<>(null, headers), ListAdvertisement.class);
        return response.getBody() == null ? new ArrayList<>() : response.getBody().getAdvertisementList();
    }
}
