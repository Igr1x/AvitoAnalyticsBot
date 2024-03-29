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

    private static final String URL = "https://api.avito.ru/core/v1/items";

    private final StatisticAvitoService statisticAvitoService;
    private final AvitoConfiguration avitoConfiguration;

    @Override
    public List<Advertisement> getAllAdvertisements(AccountData accountData) {
        int page = 1;
        List<Advertisement> result = new ArrayList<>();
        List<Advertisement> advertisementsPage = getAdvertisementsResponse(accountData,
                avitoConfiguration.getMaxAdsPerRequest(),
                page++);
        while (!advertisementsPage.isEmpty()) {
            result.addAll(advertisementsPage);
            advertisementsPage = getAdvertisementsResponse(accountData,
                    avitoConfiguration.getMaxAdsPerRequest(),
                    page++);
        }
        return result;
    }

    private List<Advertisement> getAdvertisementsResponse(AccountData accountData, Integer perPage, Integer page) {
        RestTemplate restTemplate = new RestTemplate();
        String clientToken = statisticAvitoService.getToken(accountData.getClientId(), accountData.getClientSecret());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(clientToken);
        Map<String, Object> jsonData = getParams(perPage, page);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(jsonData, headers);

        return restTemplate.postForObject(URL, request, ListAdvertisement.class).getAdvertisementList();
    }

    private Map<String, Object> getParams(Integer perPage, Integer page) {
        Map<String, Object> map = new HashMap<>();
        map.put("per_page", perPage);
        map.put("page", page);
        return map;
    }

}
