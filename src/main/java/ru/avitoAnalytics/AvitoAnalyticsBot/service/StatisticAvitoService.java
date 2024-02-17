package ru.avitoAnalytics.AvitoAnalyticsBot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.Items;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.Operations;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.Stats;

import java.util.List;

public interface StatisticAvitoService {
    List<Items> getStatistic(List<String> itemsId, String token, String userId, String dateFrom, String dateTo) throws JsonProcessingException;
    String getToken(String clientId, String clientSecret);
}
