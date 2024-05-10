package ru.avitoAnalytics.AvitoAnalyticsBot.service.impl;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AccountData;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.Advertisement;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.Items;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.Operations;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.Stats;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.*;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.SheetsStatUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class FullAdsStatisticServiceImpl implements FullAdsStatisticService {

    private final String GOOGLE_SHEETS_PREFIX = "https://docs.google.com/spreadsheets/d/";
    private final String RANGE_FOR_GET_LAST_COLUMN = "test!A%d:KI%d";

    private static String sheetName;

    private final AccountService accountService;
    private final GoogleSheetsService googleSheetsService;
    private final StatisticAvitoService statisticAvitoService;
    private final AdvertisementService advertisementService;

    @Override
    public void setStatistic() {
        List<AccountData> listAccounts = accountService.findAll();
        if (listAccounts.isEmpty()) return;
        sheetName = googleSheetsService.
        updateAccountStats(listAccounts);
    }

    private void updateAccountStats(List<AccountData> listAccounts) {
        for (AccountData account : listAccounts) {
            String token = statisticAvitoService.getToken(account.getClientId(), account.getClientSecret());
            LocalDate yesterday = LocalDate.now().minusDays(1);

            List<Advertisement> allAds = advertisementService.getAllAdvertisements(token);
            List<Long> listId = allAds.stream()
                    .map(Advertisement::getId)
                    .toList();
            List<Items> stats = statisticAvitoService.getStatistic(listId, token, account.getUserId().toString(), yesterday.toString(), yesterday.toString());

            Integer sumV = getSumStatistic(stats, Stats::getUniqViews);
            Integer sumC = getSumStatistic(stats, Stats::getUniqContacts);
            Integer sumF = getSumStatistic(stats, Stats::getUniqFavorites);
            Double allExpenses = getAllExpenses(token, yesterday);



            System.out.println("...");
        }
    }

    private double getAllExpenses(String token, LocalDate date) {
        List<Operations> operations = statisticAvitoService.getAmountExpenses(token, date, date);
        return operations.stream()
                .mapToDouble(Operations::getAmountTotal)
                .sum();
    }

    private Integer getSumStatistic(List<Items> itemsList, Function<Stats, Integer> getValue) {
        return itemsList.stream()
                .flatMap(items -> items.getStats().stream())
                .mapToInt(getValue::apply)
                .sum();
    }

    private String getLastColumn(String column) {
        char[] chars = column.toCharArray();
        int length = chars.length;
        int index = length - 1;

        while (index >= 0) {
            if (chars[index] == 'Z') {
                chars[index] = 'A';
                index--;
            } else {
                chars[index]++;
                return new String(chars);
            }
        }
        return "A" + new String(chars);
    }

    private LocalDate getDayOfStartWeek(LocalDate date) {
        LocalDate newDate = date;
        while (!SheetsStatUtil.getDayOfWeek(newDate).equals("пн")) {
            newDate = newDate.minusDays(1);
        }
        return newDate;
    }


}
