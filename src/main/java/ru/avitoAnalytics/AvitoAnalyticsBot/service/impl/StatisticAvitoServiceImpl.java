package ru.avitoAnalytics.AvitoAnalyticsBot.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.avitoAnalytics.AvitoAnalyticsBot.configuration.AvitoConfiguration;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.AvitoResponce;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.AvitoResponceOperations;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.Items;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.Operations;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.StatisticAvitoService;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.AvitoParser;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.ContactCost;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class StatisticAvitoServiceImpl implements StatisticAvitoService {
    private final static String KEY_VIEWS = "uniqViews";
    private final static String KEY_CONTACTS = "uniqContacts";
    private final static String KEY_FAVOURITES = "uniqFavorites";

    private final static String urlStats = "https://api.avito.ru/stats/v1/accounts/%s/items";
    private final static String urlToken = "https://api.avito.ru/token?client_id=%s&client_secret=%s&grant_type=client_credentials";

    private final AvitoConfiguration avitoConfiguration;

    public StatisticAvitoServiceImpl(AvitoConfiguration avitoConfiguration) {
        this.avitoConfiguration = avitoConfiguration;
    }

    @Override
    public List<Items> getStatistic(List<Long> itemsId, String token, String userId, String dateFrom, String dateTo) {
        return Lists.partition(itemsId, avitoConfiguration.getMaxItemsPerRequest())
                .parallelStream()
                .map(itemsIds -> getAvitoResponse(itemsIds, token, dateFrom, dateTo, userId))
                .flatMap(avitoResponce -> avitoResponce.getResult().getItems().stream())
                .toList();
    }

    private static AvitoResponce getAvitoResponse(List<Long> itemsId, String token, String dateFrom, String dateTo, String userId) {
        String urlRequest = String.format(urlStats, userId);
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> jsonData = getRequestParams(itemsId, dateFrom, dateTo);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(jsonData, headers);

        return rest.postForObject(urlRequest, request, AvitoResponce.class);
    }

    private static Map<String, Object> getRequestParams(List<Long> itemsId, String dateFrom, String dateTo) {
        Map<String, Object> jsonData = new HashMap<>();
        jsonData.put("dateFrom", dateFrom);
        jsonData.put("dateTo", dateTo);
        jsonData.put("fields", Arrays.asList(KEY_VIEWS, KEY_CONTACTS, KEY_FAVOURITES));
        jsonData.put("itemIds", itemsId.toArray(new Long[0]));
        jsonData.put("periodGrouping", "day");
        return jsonData;
    }

    @Override
    public String getToken(String clientId, String clientSecret) {
        String url = String.format(urlToken, clientId, clientSecret);
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded");
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(headers);
        String response = rest.postForObject(url, entity, String.class);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = null;
        try {
            node = mapper.readTree(response);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return node.get("access_token").asText();
    }

    @Override
    public Double getCost(String link) {
        List<String> parse = null;
        try {
            parse = AvitoParser.getDataForTable(link);
            return ContactCost.GetCostContact(parse);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Operations> getAmountExpenses(String token, LocalDate dateFrom, LocalDate dateTo) {
        long days = dateFrom.until(dateTo, ChronoUnit.DAYS);
        String timeFrom = "T00:00:00";
        String timeTo = "T23:59:59";
        String url = "https://api.avito.ru/core/v1/accounts/operations_history/";
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        Map<String, Object> jsonData = new HashMap<>();
        List<Operations> operations = new ArrayList<>();
        HttpEntity<Map<String, Object>> request = null;
        if (days < 7) {
            jsonData.put("dateTimeFrom", dateFrom + timeFrom);
            jsonData.put("dateTimeTo", dateTo + timeTo);
            request = new HttpEntity<>(jsonData, headers);
            AvitoResponceOperations response = rest.postForObject(url, request, AvitoResponceOperations.class);
            if (response != null) {
                operations.addAll(sortOperations(response.getResult().getOperations()));
            }
            return operations;
        }

        for (int i = 0; i <= days / 7; i++) {
            if (i == days / 7) {
                jsonData.put("dateTimeFrom", dateFrom + timeFrom);
                jsonData.put("dateTimeTo", dateTo + timeTo);
                request = new HttpEntity<>(jsonData, headers);
                AvitoResponceOperations response = rest.postForObject(url, request, AvitoResponceOperations.class);
                if (response != null) {
                    operations.addAll(sortOperations(response.getResult().getOperations()));
                    break;
                }
            }
            jsonData.put("dateTimeFrom", dateFrom + timeFrom);
            jsonData.put("dateTimeTo", dateFrom.plusDays(6) + timeTo);
            request = new HttpEntity<>(jsonData, headers);
            AvitoResponceOperations response = rest.postForObject(url, request, AvitoResponceOperations.class);
            if (response != null) {
                operations.addAll(sortOperations(response.getResult().getOperations()));
                dateFrom = dateFrom.plusDays(7);
            }
        }
        return operations;
    }

    private List<Operations> sortOperations(List<Operations> list) {
        List<Operations> newList = new ArrayList<>();
        for (Operations op : list) {
            if (op.getOperationType().equals("резервирование средств под услугу")) {
                if (op.getItemId() != null) {
                    newList.add(op);
                }
            }
        }
        for (Operations op : newList) {
            op.setUpdatedAt(op.getUpdatedAt().substring(0, 10));
        }
        return newList;
    }

    @Override
    public List<Operations> getItemOperations(List<Operations> list, Long itemId) {
        List<Operations> newList = new ArrayList<>();
        for (Operations op : list) {
            if (op.getItemId().equals(itemId.toString())) {
                newList.add(op);
            }
        }
        return newList;
    }
}
