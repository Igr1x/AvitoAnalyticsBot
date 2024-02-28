package ru.avitoAnalytics.AvitoAnalyticsBot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AccountData;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.Advertisement;
import ru.avitoAnalytics.AvitoAnalyticsBot.repositories.AccountRepository;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.AdvertisementService;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.CollectingInfoOnAdsService;

import javax.security.auth.login.AccountNotFoundException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CollectingInfoOnAdsServiceImpl implements CollectingInfoOnAdsService {

    private final AdvertisementService advertisementService;
    private final AccountRepository accountRepository;


    @Override
    public Map<String, List<Long>> getInfoOnAdvertisement(Long userId) throws AccountNotFoundException {
        AccountData accountData = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with user_id=" + userId));
        Map<String, List<Long>> advertisementsFromCity = new TreeMap<>(Comparator.naturalOrder());
        int pageCounter = 1;
        List<Advertisement> advertisementList = advertisementService.getAdvertisements(accountData,
                "100", String.valueOf(pageCounter++), "active");

        while (!advertisementList.isEmpty()) {
            for (Advertisement advertisement: advertisementList) {
                String address = advertisement.getAddress();
                String city = getCityFromAddress(address);
                Long adId = advertisement.getId();
                putAdvertisementId(advertisementsFromCity, city, adId);
            }
            advertisementList = advertisementService.getAdvertisements(accountData,
                    "100", String.valueOf(pageCounter++), "active");
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
