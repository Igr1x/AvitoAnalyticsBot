package ru.avitoAnalytics.AvitoAnalyticsBot.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AccountData;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.FavouriteItems;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.*;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Service
@AllArgsConstructor
public class SelectedAdsStatisticServiceImpl implements SelectedAdsStatisticService {
    private final String RANGE_FOR_GET_LAST_COLUMN = "test!A%d:KI%d";
    private final String RANGE_MAX_DEPTH = "test!D%d:JM%d";
    private final String GOOGLE_SHEETS_PREFIX = "https://docs.google.com/spreadsheets/d/";

    AccountService accountService;
    GoogleSheetsService googleSheetsService;
    StatisticAvitoService statisticAvitoService;
    FavouriteItemsService favouriteItemsService;

    @Override
    public void setStatistic() {
        List<AccountData> listAccounts = accountService.findAll();
        if (listAccounts.isEmpty()) return;
        updateAccountStats(listAccounts);
    }

    private List<Long> getListId(Map<String, List<AvitoItems>> map, Predicate<String> condition) {
        return map.entrySet().stream()
                .filter(x -> condition.test(x.getKey()))
                .flatMapToLong(x -> x.getValue().stream()
                        .flatMapToLong(entry -> LongStream.of(entry.getItemId())))
                .boxed()
                .toList();
    }

    private void updateAccountStats(List<AccountData> listAccounts) {
        for (AccountData account : listAccounts) {
            Map<String, List<AvitoItems>> map = googleSheetsService.getItemsWithRange(account.getSheetsRef(), RANGE_FOR_GET_LAST_COLUMN);
            String token = statisticAvitoService.getToken(account.getClientId(), account.getClientSecret());

            LocalDate dateNow = LocalDate.now();

            List<Operations> operations = statisticAvitoService.getAmountExpenses(token, dateNow.minusDays(269), dateNow);

            List<Items> statsMaxDepth = statisticAvitoService.getStatistic(getListId(map, k -> k.contains(RANGE_MAX_DEPTH)), token, account.getUserId().toString(), dateNow.minusDays(270).toString(), dateNow.minusDays(1).toString());
            List<Items> statsLastDay = statisticAvitoService.getStatistic(getListId(map, k -> !k.contains(RANGE_MAX_DEPTH)), token, account.getUserId().toString(), dateNow.minusDays(1).toString(), dateNow.minusDays(1).toString());

            updateStats(account, statsMaxDepth, operations, map, dateNow);
            updateStats(account, statsLastDay, operations, map, dateNow.minusDays(1));
        }
    }

    private void updateStats(AccountData account, List<Items> itemsList, List<Operations> operations, Map<String, List<AvitoItems>> map, LocalDate dateNow) {

        if (itemsList.isEmpty()) {
            return;
        }
        LocalDate oldestDate = getOldestDate(itemsList, dateNow.minusDays(270));

        for (Items item : itemsList) {
            Long idItem = Long.parseLong(item.getItemId());
            String range = "";
            List<Operations> itemOperations = statisticAvitoService.getItemOperations(operations, idItem);
            for (Map.Entry<String, List<AvitoItems>> entry : map.entrySet()) {
                String currentRange = entry.getKey();
                List<AvitoItems> list = entry.getValue();
                for (AvitoItems itemAvito : list) {
                    Long currentId = itemAvito.getId();
                    Long avitoId = itemAvito.getItemId();
                    if (avitoId.equals(idItem)) {
                        range = String.format(currentRange, (currentId * 15) + 1, (currentId * 15) + 10);
                        double cost = favouriteItemsService.findCostById(avitoId).doubleValue();
                        if (cost == 0) {
                            cost = statisticAvitoService.getCost(itemAvito.getSheetsLink());
                            favouriteItemsService.save(new FavouriteItems(avitoId, BigDecimal.valueOf(cost)));
                        }
                        item.setCost(cost);
                        break;
                    }
                }
                if (!range.equals("")) {
                    break;
                }
            }

            List<StatSummary> stat = new ArrayList<>();

            if (item.getStats().isEmpty()) {
                stat.add(new StatSummary(getDayOfWeek(oldestDate), oldestDate.toString()));
            } else {
                LocalDate currentDate = oldestDate;
                for (Stats stats : item.getStats()) {
                    LocalDate currentDateStats = LocalDate.parse(stats.getDate());

                    while (!currentDateStats.equals(currentDate)) {
                        stat.add(new StatSummary(getDayOfWeek(currentDate), currentDate.toString()));
                        currentDate = currentDate.plusDays(1);
                    }
                    stats.updateFields(item.getCost());

                    itemOperations.stream()
                            .filter(operation -> operation.getUpdatedAt().equals(stats.getDate()))
                            .forEach(operation -> stats.updateSumRaise(operation.getAmountTotal()));

                    stats.updateSum();
                    LocalDate date = LocalDate.parse(stats.getDate());
                    String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, new Locale("ru"));
                    stat.add(new StatSummary(dayOfWeek, stats.getDate(), stats.getUniqViews(),
                            stats.getCv(), stats.getUniqContacts(), stats.getUniqFavorites(),
                            stats.getSumViews(), stats.getSumRaise(), stats.getTotalSum(),
                            stats.getSumContact()));
                    currentDate = currentDate.plusDays(1);
                }

                while (!currentDate.equals(LocalDate.now())) {
                    stat.add(new StatSummary(getDayOfWeek(currentDate), currentDate.toString()));
                    currentDate = currentDate.plusDays(1);
                }
            }

            List<List<Object>> all = getStatSummaryMethods().stream()
                    .map(mapper -> stat.stream().map(mapper).collect(Collectors.toList()))
                    .collect(Collectors.toList());
            try {
                googleSheetsService.insertStatisticIntoTable(all, range, account.getSheetsRef().substring(GOOGLE_SHEETS_PREFIX.length()).split("/")[0]);
            } catch (IOException | GeneralSecurityException e) {
                throw new RuntimeException(e);
            }

        }
    }

    private LocalDate getFirstStatisticDay(List<Items> itemsList, LocalDate dateFrom) {
        LocalDate oldestDate = null;
        for (int i = 0; ; i++) {
            List<Stats> listStats = itemsList.get(0).getStats();
            if (!listStats.isEmpty()) {
                oldestDate = LocalDate.parse(listStats.get(0).getDate());
                break;
            }
        }

        for (Items item : itemsList) {
            if (item.getStats().isEmpty()) {
                continue;
            }
            LocalDate currentDate = LocalDate.parse(item.getStats().get(0).getDate());
            if (currentDate.isBefore(oldestDate)) {
                oldestDate = currentDate;
            }
            if (oldestDate.equals(dateFrom)) {
                break;
            }
        }
        return oldestDate;
    }

    private String getDayOfWeek(LocalDate date) {
        return date.getDayOfWeek().getDisplayName(TextStyle.SHORT, new Locale("ru"));
    }

    private LocalDate getDayOfStartWeek(LocalDate date) {
        LocalDate newDate = date;
        while (!getDayOfWeek(newDate).equals("пн")) {
            newDate = newDate.minusDays(1);
        }
        return newDate;
    }


    private LocalDate getOldestDate(List<Items> itemsList, LocalDate dateFrom) {
        LocalDate date = getFirstStatisticDay(itemsList, dateFrom);
        if (date.equals(LocalDate.now().minusDays(1)) || getDayOfWeek(date).equals("пн")) {
            return date;
        }
        date = getDayOfStartWeek(date);
        return date;
    }

    private List<Function<StatSummary, Object>> getStatSummaryMethods() {
        return List.of(StatSummary::getDayOfWeek,
                StatSummary::getDate,
                StatSummary::getUniqViews,
                StatSummary::getCv,
                StatSummary::getUniqContacts,
                StatSummary::getUniqFavorites,
                StatSummary::getSumViews,
                StatSummary::getSumRaise,
                StatSummary::getTotalSum,
                StatSummary::getSumContact);
    }
}
