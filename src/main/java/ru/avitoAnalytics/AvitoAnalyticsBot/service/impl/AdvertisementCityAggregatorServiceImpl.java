package ru.avitoAnalytics.AvitoAnalyticsBot.service.impl;

import org.springframework.stereotype.Service;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.Advertisement;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.AdvertisementAggregatorService;

import java.util.*;

@Service
public class AdvertisementCityAggregatorServiceImpl implements AdvertisementAggregatorService {

    @Override
    public Map<String, List<Long>> getInfoOnAdvertisement(List<Advertisement> advertisementList) {
        Map<String, List<Long>> advertisementsFromCity = new HashMap<>();
        for (Advertisement advertisement: advertisementList) {
            String address = advertisement.getAddress();
            String city = getCityFromAddress(address);
            Long adId = advertisement.getId();
            putAdvertisementId(advertisementsFromCity, city, adId);
        }
        return advertisementsFromCity;
    }

    private String getCityFromAddress(String address) {
        return address.split(", ")[0];
    }

    private void putAdvertisementId(Map<String, List<Long>> map, String city, Long adId) {
        if (!map.containsKey(city)) {
            map.put(city, new ArrayList<>());
            map.get(city).add(adId);
            return;
        }
        map.get(city).add(adId);
    }

}
