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
import java.time.DayOfWeek;
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
        List<AccountData> listAccounts = getAllAccounts();
        if (listAccounts.isEmpty()) return;
        updateAccountStats(listAccounts);
    }

    private List<AccountData> getAllAccounts() {
        return accountService.findAll();
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

            LocalDate dateTo = LocalDate.now();
            LocalDate dateFrom = dateTo.minusDays(1);
            DayOfWeek day = dateTo.getDayOfWeek();

            List<Operations> operations = statisticAvitoService.getAmountExpenses(token, dateTo.minusDays(269), dateTo);

            List<Items> statsMaxDepth = statisticAvitoService.getStatistic(getListId(map,k -> k.contains(RANGE_MAX_DEPTH)), token, account.getUserId().toString(), dateTo.minusDays(270).toString(), dateTo.minusDays(1).toString());
            List<Items> statsLastDay = statisticAvitoService.getStatistic(getListId(map,k -> !k.contains(RANGE_MAX_DEPTH)), token, account.getUserId().toString(), dateFrom.toString(), dateFrom.toString());

            updateStats(account, statsMaxDepth, operations, map);
            updateStats(account, statsLastDay, operations, map);
        }
    }

    private void updateStats(AccountData account, List<Items> itemsList, List<Operations> operations, Map<String, List<AvitoItems>> map) {
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
                        if (cost == 0 ) {
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
            for (Stats stats : item.getStats()) {
                stats.updateCost(item.getCost());
                stats.update();
                for (Operations operation : itemOperations) {
                    if (operation.getUpdatedAt().equals(stats.getDate())) {
                        stats.updateSumRaise(operation.getAmountTotal());
                    }
                }
                stats.updateSum();
                LocalDate date = LocalDate.parse(stats.getDate());
                String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, new Locale("ru"));
                stat.add(new StatSummary(dayOfWeek
                                ,stats.getDate(),
                                stats.getUniqViews(),
                                stats.getCv(),
                                stats.getUniqContacts(),
                                stats.getUniqFavorites(),
                                stats.getSumViews(),
                                stats.getSumRaise(),
                                stats.getTotalSum(),
                                stats.getSumContact()));
            }
            List<Function<StatSummary, Object>> mappers = List.of(
                    StatSummary::getDayOfWeek,
                    StatSummary::getDate,
                    StatSummary::getUniqViews,
                    StatSummary::getCv,
                    StatSummary::getUniqContacts,
                    StatSummary::getUniqFavorites,
                    StatSummary::getSumViews,
                    StatSummary::getSumRaise,
                    StatSummary::getTotalSum,
                    StatSummary::getSumContact
            );

            List<List<Object>> all = mappers.stream()
                    .map(mapper -> stat.stream().map(mapper).collect(Collectors.toList()))
                    .collect(Collectors.toList());
            try {
                googleSheetsService.insertStatisticIntoTable(all, range, account.getSheetsRef().substring(GOOGLE_SHEETS_PREFIX.length()).split("/")[0]);
            } catch (IOException | GeneralSecurityException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
