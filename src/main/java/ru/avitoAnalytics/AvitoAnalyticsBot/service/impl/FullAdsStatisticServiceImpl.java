package ru.avitoAnalytics.AvitoAnalyticsBot.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AccountData;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.Advertisement;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.Items;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.Operations;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.Stats;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.*;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

@Service
@AllArgsConstructor
public class FullAdsStatisticServiceImpl implements FullAdsStatisticService {
    AccountService accountService;
    GoogleSheetsService googleSheetsService;
    StatisticAvitoService statisticAvitoService;
    AdvertisementService advertisementService;

    @Override
    public void setStatistic() {
        List<AccountData> listAccounts = accountService.findAll();
        if (listAccounts.isEmpty()) return;
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
}
