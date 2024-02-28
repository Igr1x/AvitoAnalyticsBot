package ru.avitoAnalytics.AvitoAnalyticsBot.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.AvitoResponce;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.StatisticAvitoService;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.*;
import org.springframework.http.HttpHeaders;
import java.util.*;

@Service
public class StatisticAvitoServiceImpl implements StatisticAvitoService {
    private final String KEY_VIEWS = "uniqViews";
    private final String KEY_CONTACTS = "uniqContacts";
    private final String KEY_FAVOURITES = "uniqFavorites";
    private final String KEY_AUTHORIZATION = "Authorization";
    private final String VALUE_TOKEN = "Bearer %s";
    private final String KEY_CONTENT_TYPE = "Content-Type";
    private final String VALUE_APPLICATION_JSON = "application/json";

    private static String urlStats = "https://api.avito.ru/stats/v1/accounts/%s/items";
    private static String urlToken = "https://api.avito.ru/token?client_id=%s&client_secret=%s&grant_type=client_credentials";

    @Override
    public List<Items> getStatistic(List<String> itemsId, String token, String userId, String dateFrom, String dateTo) throws JsonProcessingException {
        String urlRequest = String.format(urlStats, userId);
        List<Long> itemsIdLong = itemsId.stream()
                .map(Long::parseLong)
                .toList();
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add(KEY_AUTHORIZATION, String.format(VALUE_TOKEN, token));
        headers.add(KEY_CONTENT_TYPE, VALUE_APPLICATION_JSON);
        Map<String, Object> jsonData = new HashMap<>();
        jsonData.put("dateFrom", dateFrom);
        jsonData.put("dateTo", dateTo);
        jsonData.put("fields", Arrays.asList(KEY_VIEWS, KEY_CONTACTS, KEY_FAVOURITES));
        jsonData.put("itemIds", itemsIdLong);
        jsonData.put("periodGrouping", "day");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(jsonData, headers);
        AvitoResponce responce = rest.postForObject(urlRequest, request, AvitoResponce.class);
        /*List<Operations> operations = getAmountExpenses();
        for (Stats stat : responce.getResult().getItems().get(0).getStats()) {
            for (Operations op : operations) {
                if (responce.getResult().getItems().get(0).getItemId().equals(op.getItemId()) &&
                stat.getDate().equals(op.getUpdatedAt())) {
                    stat.setTotalAmount(op.getAmountTotal());
                } else {
                    stat.setTotalAmount(0);
                }
            }
        }*/
        return responce.getResult().getItems();
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

/*    @Override
    public Double getCost() throws Exception {
        List<String> parse = AvitoParser.getDataForTable();
        return ContactCost.GetCostContact(parse);
    }*/


    /*public List<Operations> getAmountExpenses(String token, String dateFrom, String dateTo) {
        LocalDate dateTimeFrom = LocalDate.parse(dateFrom);
        LocalDate dateTimeTo = LocalDate.parse(dateTo);
        long days = dateTimeFrom.until(dateTimeTo, ChronoUnit.DAYS);
        String sh = "T00:00:00";
        String url = "https://api.avito.ru/core/v1/accounts/operations_history/";
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", String.format(VALUE_TOKEN, token));
        Map<String, Object> jsonData = new HashMap<>();
        List<Operations> operations = new ArrayList<>();
        HttpEntity<Map<String, Object>> request = null;
        if (days < 7) {
            jsonData.put("dateTimeFrom", dateFrom + sh);
            jsonData.put("dateTimeTo", dateTo + sh);
            request = new HttpEntity<>(jsonData, headers);
            AvitoResponceOperations responce = rest.postForObject(url, request, AvitoResponceOperations.class);
            operations.addAll(sortOperations(responce.getResult().getOperations()));
            return operations;
        }

        for (int i = 0; i <= days / 7; i++) {
            if (i == days / 7) {
                jsonData.put("dateTimeFrom", dateTimeFrom.toString() + sh);
                jsonData.put("dateTimeFrom", dateTo + sh);
                request = new HttpEntity<>(jsonData, headers);
                AvitoResponceOperations responce = rest.postForObject(url, request, AvitoResponceOperations.class);
                operations.addAll(sortOperations(responce.getResult().getOperations()));
                break;
            }
            jsonData.put("dateTimeFrom", dateTimeFrom.toString() + sh);
            jsonData.put("dateTimeTo", dateTimeFrom.plusDays(6).toString() + sh);
            request = new HttpEntity<>(jsonData, headers);
            AvitoResponceOperations responce = rest.postForObject(url, request, AvitoResponceOperations.class);
            operations.addAll(sortOperations(responce.getResult().getOperations()));
            dateTimeFrom = dateTimeFrom.plusDays(7);
        }
       // operations.forEach(System.out::println);
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
    }*/
}
