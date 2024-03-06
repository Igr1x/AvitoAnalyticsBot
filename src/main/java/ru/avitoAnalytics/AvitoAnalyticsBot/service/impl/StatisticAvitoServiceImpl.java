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

import java.io.IOException;
import java.security.GeneralSecurityException;
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
        String responce = rest.postForObject(url, entity, String.class);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = null;
        try {
            node = mapper.readTree(responce);
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


    public List<Operations> getAmountExpenses(String token, String dateFrom, String dateTo, Long itemId) {
        LocalDate dateTimeFrom = LocalDate.parse(dateFrom);
        LocalDate dateTimeTo = LocalDate.parse(dateTo);
        long days = dateTimeFrom.until(dateTimeTo, ChronoUnit.DAYS);
        String sh = "T00:00:00";
        String url = "https://api.avito.ru/core/v1/accounts/operations_history/";
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", String.format("Bearer ", token));
        Map<String, Object> jsonData = new HashMap<>();
        List<Operations> operations = new ArrayList<>();
        HttpEntity<Map<String, Object>> request = null;
        if (days < 7) {
            jsonData.put("dateTimeFrom", dateFrom + sh);
            jsonData.put("dateTimeTo", dateTo + sh);
            request = new HttpEntity<>(jsonData, headers);
            AvitoResponceOperations responce = rest.postForObject(url, request, AvitoResponceOperations.class);
            operations.addAll(sortOperations(responce.getResult().getOperations(), itemId.toString()));
            return operations;
        }

        for (int i = 0; i <= days / 7; i++) {
            if (i == days / 7) {
                jsonData.put("dateTimeFrom", dateTimeFrom.toString() + sh);
                jsonData.put("dateTimeFrom", dateTo + sh);
                request = new HttpEntity<>(jsonData, headers);
                AvitoResponceOperations responce = rest.postForObject(url, request, AvitoResponceOperations.class);
                operations.addAll(sortOperations(responce.getResult().getOperations(), itemId.toString()));
                break;
            }
            jsonData.put("dateTimeFrom", dateTimeFrom.toString() + sh);
            jsonData.put("dateTimeTo", dateTimeFrom.plusDays(6).toString() + sh);
            request = new HttpEntity<>(jsonData, headers);
            AvitoResponceOperations responce = rest.postForObject(url, request, AvitoResponceOperations.class);
            operations.addAll(sortOperations(responce.getResult().getOperations(), itemId.toString()));
            dateTimeFrom = dateTimeFrom.plusDays(7);
        }
        return operations;
    }

    private List<Operations> sortOperations(List<Operations> list, String itemId) {
        List<Operations> newList = new ArrayList<>();
        for (Operations op : list) {
            if (op.getOperationType().equals("резервирование средств под услугу")) {
                if (op.getItemId() != null && op.getItemId().equals(itemId)){
                    newList.add(op);
                }
            }
        }
        for (Operations op : newList) {
            op.setUpdatedAt(op.getUpdatedAt().substring(0, 10));
        }
        newList.forEach(System.out::println);
        return newList;
    }
}
