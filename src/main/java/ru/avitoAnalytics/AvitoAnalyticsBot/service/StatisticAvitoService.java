package ru.avitoAnalytics.AvitoAnalyticsBot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.Items;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.Operations;

import java.time.LocalDate;
import java.util.List;

public interface StatisticAvitoService {
    List<Items> getStatistic(List<Long> itemsId, String token, String userId, String dateFrom, String dateTo);
    String getToken(String clientId, String clientSecret) throws JsonProcessingException;
    //Double getCost(String link);
    List<Operations> getAmountExpenses(String token, LocalDate dateFrom, LocalDate dateTo);
    List<Operations> getItemOperations(List<Operations> list, Long itemId);
}
