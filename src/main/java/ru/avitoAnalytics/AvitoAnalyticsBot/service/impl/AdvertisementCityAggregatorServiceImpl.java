package ru.avitoAnalytics.AvitoAnalyticsBot.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AccountData;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.Ads;
import ru.avitoAnalytics.AvitoAnalyticsBot.exceptions.AvitoResponseException;
import ru.avitoAnalytics.AvitoAnalyticsBot.exceptions.GoogleSheetsInsertException;
import ru.avitoAnalytics.AvitoAnalyticsBot.exceptions.GoogleSheetsReadException;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.Items;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.Stats;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdvertisementCityAggregatorServiceImpl implements AdvertisementAggregatorService {

    private final static String NAME_TABLE = "StatCity#";
    private final static String RIGHT_RANGE = "A";
    private final static String LEFT_RANGE = "G";
    private final static int PARAM = 7;


    private final AccountService accountService;
    private final StatisticAvitoService statisticAvitoService;
    private final AdvertisementAvitoService advertisementAvitoService;
    private final GoogleSheetsService googleSheetsService;
    private final AdsService adsService;

    @Override
    public Map<String, List<Long>> getInfoOnAdvertisement(List<Ads> advertisementList) {
        Map<String, List<Long>> advertisementsFromCity = new HashMap<>();
        for (Ads advertisement : advertisementList) {
            String address = advertisement.getCity();
            Long adId = advertisement.getAvitoId();
            List<Long> adIds = advertisementsFromCity.computeIfAbsent(address, k -> new ArrayList<>());
            adIds.add(adId);
        }
        return advertisementsFromCity;
    }

    @Override
    public void fillingStatisticCities(AccountData accountData) {
        String accountName = accountData.getAccountName();
        LocalDate dateTo = null;
        LocalDate dateFrom = null;
        String leftColumn = LEFT_RANGE;
        String rightColumn = RIGHT_RANGE;
        String sheetsName = null;

        for (int i = 0; i < 5; i++) {
            for (int j = 1; j < 8; j++) {
                leftColumn = googleSheetsService.getNextColumn(leftColumn);
            }
            for (int j = 1; j < 8; j++) {
                rightColumn = googleSheetsService.getNextColumn(rightColumn);
            }
            if (i == 0) {
                leftColumn = LEFT_RANGE;
                rightColumn = RIGHT_RANGE;
            }
            try {
                sheetsName = googleSheetsService.getSheetByName(NAME_TABLE, accountData.getSheetsRef()).orElseThrow(() -> new GoogleSheetsReadException("Sheet not found"));
                String range = sheetsName + "!" + rightColumn + "1:" + leftColumn + "1";
                var data = googleSheetsService.getDataFromTable(accountData.getSheetsRef(), range).get(0);
                String fName = data.get(1).toString();
                if (!fName.equals(accountName)) {
                    continue;
                } else {
                    dateFrom = LocalDate.parse(data.get(3).toString());
                    dateTo = LocalDate.parse(data.get(4).toString());
                    break;
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                break;
            }
        }

        if (dateFrom == null || dateTo == null || sheetsName == null) {
            return;
        }


        String token = null;
        try {
            token = statisticAvitoService.getToken(accountData.getClientId(), accountData.getClientSecret());
        } catch (JsonProcessingException | AvitoResponseException e) {
            log.error("Error: while getting token for account {}, error message: {}", accountData.toString(), e.getMessage());
        }
        List<Ads> allAds = adsService.findAdsFilter(accountData, dateFrom);
        Map<String, List<Long>> advertFromCities = getInfoOnAdvertisement(allAds);
        String userId = String.valueOf(accountData.getUserId());
        String range = sheetsName + "!" + rightColumn + "3:" + leftColumn + "500";
        List<List<Object>> statsCities = new ArrayList<>();
        for (String city : advertFromCities.keySet()) {
            List<Object> statsCity = new ArrayList<>();
            List<Long> ids = advertFromCities.get(city);
            double cost = 0.0d;
            for (Long id : ids) {
                cost += adsService.findCostByAvitoId(id).orElse(BigDecimal.ZERO).doubleValue();
            }
            cost = cost / ids.size();
            List<Items> stats = statisticAvitoService.getStatistic(ids, token, userId, dateFrom.toString(), dateTo.toString());
            statsCity.add(city);
            statsCity.add(getSumStatisticInt(stats, Stats::getUniqViews));
            statsCity.add("=ЕСЛИ(ИЛИ(ЯЧЕЙКА(\"contents\"; ДВССЫЛ(АДРЕС(СТРОКА();СТОЛБЕЦ()-1)))=0; ЯЧЕЙКА(\"contents\"; ДВССЫЛ(АДРЕС(СТРОКА();СТОЛБЕЦ()+1)))=0; ЯЧЕЙКА(\"contents\"; ДВССЫЛ(АДРЕС(СТРОКА();СТОЛБЕЦ()-1)))=\"\"; ЯЧЕЙКА(\"contents\"; ДВССЫЛ(АДРЕС(СТРОКА();СТОЛБЕЦ()+1)))=\"\"); 0; ЯЧЕЙКА(\"contents\"; ДВССЫЛ(АДРЕС(СТРОКА();СТОЛБЕЦ()+1))) / ЯЧЕЙКА(\"contents\"; ДВССЫЛ(АДРЕС(СТРОКА();СТОЛБЕЦ()-1))))");
            statsCity.add(getSumStatisticInt(stats, Stats::getUniqContacts));
            statsCity.add(getSumStatisticInt(stats, Stats::getUniqFavorites));
            statsCity.add(getSumStatisticDouble(stats, Stats::getTotalSum));
            statsCity.add("=" + cost + " * ЕСЛИ(ИЛИ(ЯЧЕЙКА(\"contents\"; ДВССЫЛ(АДРЕС(СТРОКА();СТОЛБЕЦ()-1)))=0; ЯЧЕЙКА(\"contents\"; ДВССЫЛ(АДРЕС(СТРОКА();СТОЛБЕЦ()-3)))=0; ЯЧЕЙКА(\"contents\"; ДВССЫЛ(АДРЕС(СТРОКА();СТОЛБЕЦ()-1)))=\"\"; ЯЧЕЙКА(\"contents\"; ДВССЫЛ(АДРЕС(СТРОКА();СТОЛБЕЦ()-3)))=\"\"); 0; ЯЧЕЙКА(\"contents\"; ДВССЫЛ(АДРЕС(СТРОКА();СТОЛБЕЦ()-1))) / ЯЧЕЙКА(\"contents\"; ДВССЫЛ(АДРЕС(СТРОКА();СТОЛБЕЦ()-3))))");
            statsCities.add(statsCity);
        }
        try {
            googleSheetsService.insertStatisticIntoTable(statsCities, range, accountData.getSheetsRef());
        } catch (GoogleSheetsReadException | GoogleSheetsInsertException e) {
            log.error(e.getMessage());
            log.error(e.getCause().getMessage());
        }
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
