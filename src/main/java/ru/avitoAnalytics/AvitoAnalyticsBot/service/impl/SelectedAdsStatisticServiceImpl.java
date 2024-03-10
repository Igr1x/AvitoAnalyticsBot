package ru.avitoAnalytics.AvitoAnalyticsBot.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AccountData;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.AvitoItems;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.Items;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.Operations;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.Stats;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.AccountService;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.GoogleSheetsService;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.SelectedAdsStatisticService;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.StatisticAvitoService;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.ContactCost;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Service
@AllArgsConstructor
public class SelectedAdsStatisticServiceImpl implements SelectedAdsStatisticService {
    private final String RANGE_FOR_GET_LAST_COLUMN = "test!A%d:KI%d";

    AccountService accountService;
    GoogleSheetsService googleSheetsService;
    StatisticAvitoService statisticAvitoService;

    @Override
    public void SetStatistic() {
        List<AccountData> listAccounts = getAllAccounts();
        if (listAccounts.isEmpty()) return;
        for (AccountData account : listAccounts) {
            List<String> links = googleSheetsService.getLinksIdFavouriteItems(account.getSheetsRef());
            Map<String, List<AvitoItems>> map = googleSheetsService.getItemsWithRange(account.getSheetsRef(), RANGE_FOR_GET_LAST_COLUMN);
            LocalDate dateTo = LocalDate.now();
            LocalDate dateFrom = dateTo.minusDays(1);


            List<Long> idAllStat = map.entrySet().stream()
                    .filter(x -> x.getKey().contains("test!D%d:JM%d"))
                    .flatMapToLong(x -> x.getValue().stream()
                            .flatMapToLong(entry -> LongStream.of(entry.getItemId())))
                    .boxed()
                    .toList();

            List<Long> id = map.entrySet().stream()
                    .filter(x -> !x.getKey().contains("test!D%d:JM%d"))
                    .flatMapToLong(x -> x.getValue().stream()
                            .flatMapToLong(entry -> LongStream.of(entry.getItemId())))
                    .boxed()
                    .toList();

            String token = statisticAvitoService.getToken(account.getClientId(), account.getClientSecret());
            List<Items> listStats = statisticAvitoService.getStatistic(idAllStat, token, account.getUserId().toString(), dateTo.minusDays(270).toString(), dateTo.minusDays(1).toString());
            List<Items> listSt = statisticAvitoService.getStatistic(id, token, account.getUserId().toString(), dateFrom.toString(), dateFrom.toString());
            List<Operations> operations = statisticAvitoService.getAmountExpenses(token, dateTo.minusDays(269), dateTo);

            for (Items item : listStats) {
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
                            range = String.format(currentRange, (currentId * 15) + 2, (currentId * 15) + 10);
                            item.setCost(statisticAvitoService.getCost(itemAvito.getSheetsLink()));
                            break;
                        }
                    }
                    if (!range.equals("")) {
                        break;
                    }
                }
                List<Object> date = new ArrayList<>();
                List<Object> statsViews = new ArrayList<>();
                List<Object> cv = new ArrayList<>();
                List<Object> statsFavourite = new ArrayList<>();
                List<Object> statsContacts = new ArrayList<>();
                List<Object> statsSumViews = new ArrayList<>();
                List<Object> statsSumRaise = new ArrayList<>();
                List<Object> statsTotalSum = new ArrayList<>();
                List<Object> statsSumContact = new ArrayList<>();
                for (Stats stats : item.getStats()) {
                    stats.updateCost(item.getCost());
                    stats.update();
                    date.add(stats.getDate());
                    statsViews.add(stats.getUniqViews());
                    cv.add(stats.getCv());
                    statsContacts.add(stats.getUniqContacts());
                    statsFavourite.add(stats.getUniqFavorites());
                    statsSumViews.add(stats.getSumViews());
                    for (Operations operation : itemOperations) {
                        if (operation.getUpdatedAt().equals(stats.getDate())) {
                            stats.updateSumRaise(operation.getAmountTotal());
                        }
                    }
                    stats.updateSum();
                    statsSumRaise.add(stats.getSumRaise());
                    statsTotalSum.add(stats.getTotalSum());
                    statsSumContact.add(stats.getSumContact());
                }
                List<List<Object>> all = List.of(date,
                        statsViews,
                        cv,
                        statsContacts,
                        statsFavourite,
                        statsSumViews,
                        statsSumRaise,
                        statsTotalSum,
                        statsSumContact);
                try {
                    googleSheetsService.insertStatisticIntoTable(all, range, account.getSheetsRef().substring("https://docs.google.com/spreadsheets/d/".length()).split("/")[0]);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (GeneralSecurityException e) {
                    throw new RuntimeException(e);
                }
            }
            for (Items item : listSt) {
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
                            range = String.format(currentRange, (currentId * 15) + 2, (currentId * 15) + 10);
                            item.setCost(statisticAvitoService.getCost(itemAvito.getSheetsLink()));
                            break;
                        }
                    }
                    if (!range.equals("")) {
                        break;
                    }
                }
                List<Object> date = new ArrayList<>();
                List<Object> statsViews = new ArrayList<>();
                List<Object> cv = new ArrayList<>();
                List<Object> statsFavourite = new ArrayList<>();
                List<Object> statsContacts = new ArrayList<>();
                List<Object> statsSumViews = new ArrayList<>();
                List<Object> statsSumRaise = new ArrayList<>();
                List<Object> statsTotalSum = new ArrayList<>();
                List<Object> statsSumContact = new ArrayList<>();
                for (Stats stats : item.getStats()) {
                    stats.updateCost(item.getCost());
                    stats.update();
                    date.add(stats.getDate());
                    statsViews.add(stats.getUniqViews());
                    cv.add(stats.getCv());
                    statsContacts.add(stats.getUniqContacts());
                    statsFavourite.add(stats.getUniqFavorites());
                    statsSumViews.add(stats.getSumViews());
                    Iterator<Operations> iterator = itemOperations.iterator();
                    while (iterator.hasNext()) {
                        Operations operation = iterator.next();
                        if (operation.getUpdatedAt().equals(stats.getDate())) {
                            stats.updateSumRaise(operation.getAmountTotal());
                            iterator.remove();
                        }
                    }
                    stats.updateSum();
                    statsSumRaise.add(stats.getSumRaise());
                    statsTotalSum.add(stats.getTotalSum());
                    statsSumContact.add(stats.getSumContact());
                }
                List<List<Object>> all = List.of(date,
                        statsViews,
                        cv,
                        statsContacts,
                        statsFavourite,
                        statsSumViews,
                        statsSumRaise,
                        statsTotalSum,
                        statsSumContact);
                try {
                    googleSheetsService.insertStatisticIntoTable(all, range, account.getSheetsRef().substring("https://docs.google.com/spreadsheets/d/".length()).split("/")[0]);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (GeneralSecurityException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private List<AccountData> getAllAccounts() {
        return accountService.findAll();
    }

}
