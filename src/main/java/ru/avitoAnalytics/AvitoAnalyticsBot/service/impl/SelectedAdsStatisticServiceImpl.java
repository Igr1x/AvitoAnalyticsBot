package ru.avitoAnalytics.AvitoAnalyticsBot.service.impl;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AccountData;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.FavouriteItems;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.*;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.*;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.SheetsStatUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Service
@AllArgsConstructor
public class SelectedAdsStatisticServiceImpl implements SelectedAdsStatisticService {
    private final String RANGE_FOR_GET_LAST_COLUMN = "%s!A%%d:KI%%d";
    private final String RANGE_MAX_DEPTH = "%s!D%d:RH%d";
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
            String title = googleSheetsService.getSheetByName("StatFav#", account.getSheetsRef().substring(GOOGLE_SHEETS_PREFIX.length()).split("/")[0]).get();
            Map<String, List<AvitoItems>> map = googleSheetsService.getItemsWithRange(account.getSheetsRef(), String.format(RANGE_FOR_GET_LAST_COLUMN, title), title, 15, 3);
            if (map.isEmpty()) {
                continue;
            }
            String token = statisticAvitoService.getToken(account.getClientId(), account.getClientSecret());

            LocalDate dateNow = LocalDate.now();

            List<Operations> operations = statisticAvitoService.getAmountExpenses(token, dateNow.minusDays(269), dateNow);

            StringBuilder maxRange = new StringBuilder(title + '!' + "D%d:RH%d");

            List<Items> statsMaxDepth = statisticAvitoService.getStatistic(getListId(map, k -> k.contains(maxRange)), token, account.getUserId().toString(), dateNow.minusDays(270).toString(), dateNow.minusDays(1).toString());
            List<Items> statsLastDay = statisticAvitoService.getStatistic(getListId(map, k -> !k.contains(maxRange)), token, account.getUserId().toString(), dateNow.minusDays(1).toString(), dateNow.minusDays(1).toString());

            updateStats(account, statsMaxDepth, operations, map, dateNow, title);
            updateStats(account, statsLastDay, operations, map, dateNow.minusDays(1), title);
        }
    }

    private void setDatesAndDayOfWeek(AccountData account, LocalDate day, String range, int days, String tittle) {
        LocalDate lastDate = day.plusDays(days);
        List<Object> date = new ArrayList<>();
        List<Object> dayOfWeek = new ArrayList<>();
        List<List<Object>> daysList = new ArrayList<>();
        while (!day.equals(lastDate)) {
            date.add(day.toString());
            dayOfWeek.add(SheetsStatUtil.getDayOfWeek(day));
            if (SheetsStatUtil.getDayOfWeek(day).equals("вс")) {
                date.add("Итог недели");
                dayOfWeek.add(day.minusDays(6) + "-" + day);
            }
            day = day.plusDays(1);
        }
        daysList.add(dayOfWeek);
        daysList.add(date);

        String newRange = createRangeForDate(range, days, tittle);
        try {
            googleSheetsService.insertStatisticIntoTable(daysList, newRange, account.getSheetsRef().substring(GOOGLE_SHEETS_PREFIX.length()).split("/")[0]);
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    private String createRangeForDate(String range, int days, String tittle) {
        Matcher matcher = getMatcherForCololumn(range);
        String first = null;
        int value = 0;
        if (matcher.find()) {
            first = matcher.group(1);
            value = Integer.parseInt(matcher.group(2));
        }
        String last = first;

        String newRangeGen = "%s!%s%d:%s%d";

        days = days + 10;
        for (int i = 0; i <= days; i++) {
            last = getLastColumn(last);
        }
        String newRange = String.format(newRangeGen, tittle, first, value, last, ++value);
        if (days == 375) {
            newRange = String.format(RANGE_MAX_DEPTH, tittle, --value, ++value);
        }
        return newRange;
    }

    private Matcher getMatcherForCololumn(String range) {
        return Pattern.compile("!([A-Z]+)([0-9]+)").matcher(range);
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

    private void updateStats(AccountData account, List<Items> itemsList, List<Operations> operations, Map<String, List<AvitoItems>> map, LocalDate dateNow, String tittle) {
        if (itemsList.isEmpty()) {
            return;
        }
        LocalDate oldestDate = getOldestDate(itemsList, dateNow.minusDays(270));
        if (dateNow.equals(LocalDate.now())) {
            oldestDate = googleSheetsService.getOldestDate(account.getSheetsRef(), tittle).orElse(oldestDate);
        }
        for (Items item : itemsList) {
            item = setRangeAndCost(map, item);
            insertDate(account, item, oldestDate, tittle);
            List<StatSummary> stat = new ArrayList<>();
            if (item.getStats().isEmpty()) {
                stat.add(new StatSummary(SheetsStatUtil.getDayOfWeek(oldestDate), oldestDate.toString()));
                if (SheetsStatUtil.getDayOfWeek(oldestDate).equals("вс")) {
                    stat.addAll(SheetsStatUtil.setStatsWeek(oldestDate));
                }
            } else {
                stat.addAll(getStatsList(item, operations, oldestDate));
            }

            List<List<Object>> all = getStatSummaryMethods().stream()
                    .map(mapper -> stat.stream().
                            map(mapper)
                            .collect(Collectors.toList()))
                    .collect(Collectors.toList());
            try {
                googleSheetsService.insertStatisticIntoTable(all, item.getRange(), account.getSheetsRef().substring(GOOGLE_SHEETS_PREFIX.length()).split("/")[0]);
            } catch (IOException | GeneralSecurityException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void insertDate(AccountData account, Items item, LocalDate oldestDate, String tittle) {
        Matcher matcher = getMatcherForCololumn(item.getRange());
        String first = null;
        if (matcher.find()) {
            first = matcher.group(1);
        }
        int quantityDays = 365;
        if (!first.equals("D")) {
            quantityDays = 30;
        }
        setDatesAndDayOfWeek(account, oldestDate, item.getRange(), quantityDays, tittle);
    }


    private List<StatSummary> getStatsList(Items item, List<Operations> operations, LocalDate oldestDate) {
        Long idItem = Long.parseLong(item.getItemId());
        List<Operations> itemOperations = statisticAvitoService.getItemOperations(operations, idItem);
        LocalDate currentDate = oldestDate;
        List<StatSummary> statsList = new ArrayList<>();
        for (Stats stats : item.getStats()) {
            LocalDate currentDateStats = LocalDate.parse(stats.getDate());
            statsList.addAll(getMissingDayStats(currentDateStats, currentDate).getKey());

            currentDate = getMissingDayStats(currentDateStats, currentDate).getValue();

            itemOperations.stream()
                    .filter(operation -> operation.getUpdatedAt().equals(stats.getDate()))
                    .forEach(operation -> stats.updateSumRaise(operation.getAmountTotal()));

            stats.updateFields(item.getCost());

            LocalDate date = LocalDate.parse(stats.getDate());
            String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, new Locale("ru"));
            statsList.add(new StatSummary(dayOfWeek, stats.getDate(), stats.getUniqViews(),
                    stats.getCv(), stats.getUniqContacts(), stats.getUniqFavorites(),
                    stats.getSumViews(), stats.getSumRaise(), stats.getTotalSum(),
                    stats.getSumContact()));
            if (dayOfWeek.equals("вс")) {
                item.getRange();
                statsList.addAll(SheetsStatUtil.setStatsWeek(LocalDate.parse(stats.getDate())));
            }
            currentDate = currentDate.plusDays(1);
        }
        statsList.addAll(getMissingDayStats(LocalDate.now(), currentDate).getKey());
        return statsList;
    }

    private Pair<List<StatSummary>, LocalDate> getMissingDayStats(LocalDate currentDateStats, LocalDate date) {
        List<StatSummary> missingDayStats = new ArrayList<>();
        while (!currentDateStats.equals(date)) {
            missingDayStats.add(new StatSummary(SheetsStatUtil.getDayOfWeek(date), date.toString()));
            if (SheetsStatUtil.getDayOfWeek(date).equals("вс")) {
                missingDayStats.addAll(SheetsStatUtil.setStatsWeek(date));
            }
            date = date.plusDays(1);
        }
        return Pair.of(missingDayStats, date);
    }

    private LocalDate getFirstStatisticDay(List<Items> itemsList, LocalDate dateFrom) {
        LocalDate oldestDate = dateFrom;
        for (int i = 0; i < itemsList.size(); i++) {
            List<Stats> listStats = itemsList.get(i).getStats();
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

    private LocalDate getOldestDate(List<Items> itemsList, LocalDate dateFrom) {
        LocalDate date = getFirstStatisticDay(itemsList, dateFrom);
        if (date.equals(LocalDate.now().minusDays(1)) || SheetsStatUtil.getDayOfWeek(date).equals("пн")) {
            return date;
        }
        date = SheetsStatUtil.getDayOfStartWeek(date);
        return date;
    }

    private Items setRangeAndCost(Map<String, List<AvitoItems>> map, Items item) {
        Long idItem = Long.parseLong(item.getItemId());
        for (Map.Entry<String, List<AvitoItems>> entry : map.entrySet()) {
            String currentRange = entry.getKey();
            List<AvitoItems> avitoItemsList = entry.getValue();
            for (AvitoItems itemAvito : avitoItemsList) {
                Long currentSheetId = itemAvito.getId();
                Long avitoId = itemAvito.getItemId();
                if (avitoId.equals(idItem)) {
                    item.setRange(String.format(currentRange, (currentSheetId * 15) + 1, (currentSheetId * 15) + 10));
                    item.setCost(0.0);
                    item.setSheetsLink(itemAvito.getSheetsLink());
                    break;
                }
            }
            if (item.getRange() != null) {
                break;
            }
        }
        return item;
    }

    private double getCostForItem(AvitoItems item) {
        Long avitoId = item.getItemId();
        double cost = favouriteItemsService.findCostById(avitoId).doubleValue();
        if (cost == 0) {
            cost = statisticAvitoService.getCost(item.getSheetsLink());
            favouriteItemsService.save(new FavouriteItems(avitoId, BigDecimal.valueOf(cost)));
        }
        return cost;
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
