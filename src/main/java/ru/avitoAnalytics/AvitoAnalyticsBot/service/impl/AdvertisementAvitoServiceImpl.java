package ru.avitoAnalytics.AvitoAnalyticsBot.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.avitoAnalytics.AvitoAnalyticsBot.configuration.AvitoConfiguration;
import ru.avitoAnalytics.AvitoAnalyticsBot.exceptions.AdvertisementServiceException;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.Advertisement;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.ListAdvertisement;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.AdvertisementAvitoService;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdvertisementAvitoServiceImpl implements AdvertisementAvitoService {
    ///*,old,rejected,removed,blocked*/
    //private static final String URL = "https://api.avito.ru/core/v1/items?&page=%s&per_page=%s&updatedAtFrom=%s";
    private static final String URL = "https://api.avito.ru/core/v1/items?&page=%s&per_page=%s";

    private final AvitoConfiguration avitoConfiguration;

    @Override
    public List<Advertisement> getAllAdvertisements(String token, String updatedAtFrom) {
        int page = 1;
        List<Advertisement> result = new ArrayList<>();
        List<Advertisement> advertisementsPage = getAdvertisementsResponse(token,
                avitoConfiguration.getMaxAdsPerRequest(),
                page++, updatedAtFrom);
        while (!advertisementsPage.isEmpty()) {
            result.addAll(advertisementsPage);
            try {
                advertisementsPage = getAdvertisementsResponse(token,
                        avitoConfiguration.getMaxAdsPerRequest(),
                        page++, updatedAtFrom);
            } catch (RestClientException e) {
                throw new AdvertisementServiceException(String.format("Error: get all ads, page - %d", page), e);
            }
        }
        return result;
    }

    private List<Advertisement> getAdvertisementsResponse(String token, Integer perPage, Integer page, String updatedAtFrom) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        String currentUrl = String.format(URL, page, perPage);
        var response = restTemplate.exchange(currentUrl, HttpMethod.GET, new HttpEntity<>(null, headers), ListAdvertisement.class);
        return response.getBody() == null ? new ArrayList<>() : response.getBody().getAdvertisementList();
    }
}
