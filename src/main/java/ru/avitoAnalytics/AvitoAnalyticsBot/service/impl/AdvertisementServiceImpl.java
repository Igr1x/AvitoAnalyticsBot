package ru.avitoAnalytics.AvitoAnalyticsBot.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
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
    private static final String TEMPLATE_CLIENT_TOKEN_FOR_AUTHORIZATION = "Bearer %s";

    private final StatisticAvitoService statisticAvitoService;
    private final RestTemplateWork restTemplateWork;

    @Override
    public List<Advertisement> getAdvertisements(AccountData accountData, String perPage, String page, String statusAdvertisement) {
        try {
            MultiValueMap<String, String> map = restTemplateWork.getParams(perPage, page, statusAdvertisement);
            String clientToken = String.format(TEMPLATE_CLIENT_TOKEN_FOR_AUTHORIZATION,
                    statisticAvitoService.getToken(accountData.getClientId(), accountData.getClientSecret()));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.add("Authorization", clientToken);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplateWork.getResponse(URL, map, request);
            ObjectMapper mapper = new ObjectMapper();
            ListAdvertisement listAdvertisement = mapper.readValue(response.getBody(), ListAdvertisement.class);
            return listAdvertisement.getAdvertisementList();
        } catch (JsonProcessingException e) {
            throw new RuntimeException();
        }
    }

}
