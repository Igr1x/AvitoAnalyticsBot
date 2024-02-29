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
            advertisementsFromCity.getOrDefault(city, new ArrayList<>()).add(adId);
        }
        return advertisementsFromCity;
    }

    private String getCityFromAddress(String address) {
        return address.split(", ")[0];
    }

}
