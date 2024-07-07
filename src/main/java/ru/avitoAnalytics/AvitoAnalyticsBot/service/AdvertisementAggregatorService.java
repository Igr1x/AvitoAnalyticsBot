package ru.avitoAnalytics.AvitoAnalyticsBot.service;

import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AccountData;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.Advertisement;

import java.util.List;
import java.util.Map;

public interface AdvertisementAggregatorService {

    Map<String, List<Long>> getInfoOnAdvertisement(List<Advertisement> advertisementList);
    public void fillingStatisticCities(AccountData accountData);

}
