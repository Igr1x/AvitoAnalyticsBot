package ru.avitoAnalytics.AvitoAnalyticsBot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AccountData;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.Advertisement;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.Items;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.Stats;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class AdvertisementCityAggregatorServiceImpl implements AdvertisementAggregatorService {

    private final AccountService accountService;
    private final StatisticAvitoService statisticAvitoService;
    private final AdvertisementService advertisementService;
    private final GoogleSheetsService googleSheetsService;

    private final String GOOGLE_SHEETS_PREFIX = "https://docs.google.com/spreadsheets/d/";

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

    @Override
    public void fillingStatisticCities(String dateFrom, String dateTo) {
        List<AccountData> listAccounts = accountService.findAll();
        if (listAccounts.isEmpty()) return;
        for (AccountData accountData: listAccounts) {
            String token = statisticAvitoService.getToken(accountData.getClientId(), accountData.getClientSecret());
            List<Advertisement> advertisementList = advertisementService.getAllAdvertisements(token, dateFrom);
            Map<String, List<Long>> advertFromCities = getInfoOnAdvertisement(advertisementList);
            String userId = String.valueOf(accountData.getUserId());
            String range = "Города!A3:G200";
            List<List<Object>> statsCities = new ArrayList<>();
            for (String city: advertFromCities.keySet()) {
                List<Object> statsCity = new ArrayList<>();
                List<Items> stats = statisticAvitoService.getStatistic(advertFromCities.get(city), token, userId, dateFrom, dateTo);
                statsCity.add(city);
                statsCity.add(getSumStatisticInt(stats, Stats::getUniqViews));
                statsCity.add(getSumStatisticDouble(stats, Stats::getCv));
                statsCity.add(getSumStatisticInt(stats, Stats::getUniqContacts));
                statsCity.add(getSumStatisticInt(stats, Stats::getUniqFavorites));
                statsCity.add(getSumStatisticDouble(stats, Stats::getTotalSum));
                statsCity.add(getSumStatisticDouble(stats, Stats::getSumContact));
                statsCities.add(statsCity);
            }
            try {
                googleSheetsService.insertStatisticIntoTable(statsCities, range, accountData.getSheetsRef().substring(GOOGLE_SHEETS_PREFIX.length()).split("/")[0]);
            } catch (IOException | GeneralSecurityException e) {
                e.printStackTrace();
            }
        }
    }

    private String getCityFromAddress(String address) {
        return address.split(", ")[0];
    }

    private Integer getSumStatisticInt(List<Items> itemsList, Function<Stats, Integer> getValue) {
        return itemsList.stream()
                .flatMap(items -> items.getStats().stream())
                .mapToInt(getValue::apply)
                .sum();
    }

    private Double getSumStatisticDouble(List<Items> itemsList, Function<Stats, Double> getValue) {
        return itemsList.stream()
                .flatMap(items -> items.getStats().stream())
                .mapToDouble(getValue::apply)
                .sum();
    }

}
