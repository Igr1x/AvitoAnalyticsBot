package ru.avitoAnalytics.AvitoAnalyticsBot.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.avitoAnalytics.AvitoAnalyticsBot.controller.ParserProcessor;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AccountData;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.Ads;
import ru.avitoAnalytics.AvitoAnalyticsBot.exceptions.*;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.*;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.*;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.SheetsStatUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FullAdsStatisticServiceImpl implements FullAdsStatisticService {

    private final String RANGE_FOR_GET_LAST_COLUMN = "%s!A%%d:ZZZ%%d";

    private final AccountService accountService;
    private final GoogleSheetsService googleSheetsService;
    private final StatisticAvitoService statisticAvitoService;
    private final AdvertisementAvitoService advertisementAvitoService;
    private final ParserProcessor parserProcessor;
    private final AdsService adsService;

    public static String name;

    @Override
    @Scheduled(cron = "0 0 1 * * *")
    @Async
    public void setStatistic() {
        var listLinks = accountService.findAll().stream()
                .filter(account -> {
                    var userOwner = account.getUserOwner();
                    return userOwner != null && userOwner.getRate().getId() != 4;
                })
                .map(AccountData::getSheetsRef)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (listLinks.isEmpty()) {
            return;
        }
        try {
            updateAccountStats(listLinks);
        } catch (SheetsNotExistedException e) {

        }
    }

    private void updateAccountStats(List<String> listSheetsRef) {
        for (String sheetRef : listSheetsRef) {
            String sheetName;
            try {
                sheetName = googleSheetsService.getSheetByName("StatAcc#", sheetRef)
                        .orElseThrow(() -> new SheetsNotExistedException(String.format("Not found sheet by name, sheet id %s", sheetRef)));
            } catch (SheetsNotExistedException e) {
                log.warn(e.getMessage());
                continue;
            }
            name = sheetName;
            try {
                var sheetWithRangeMap = googleSheetsService.getAccountsWithRange(sheetRef, String.format(RANGE_FOR_GET_LAST_COLUMN, sheetName), sheetName);
                setAccountStats(sheetWithRangeMap);
            } catch (GoogleSheetsReadException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private void setAccountStats(Map<String, List<String>> listSheetsRef) {
        for (Map.Entry<String, List<String>> entry : listSheetsRef.entrySet()) {
            AccountData account = null;
            try {
                var accountName = entry.getValue().get(0).trim();
                account = accountService.findByAccountName(accountName)
                        .orElseThrow(() -> new AccountNotFoundException(String.format("Account %s not found", entry.getValue().get(0))));
            } catch (AccountNotFoundException e) {
                log.warn(e.getMessage());
                continue;
            }

            if (!account.isReport()) {
                continue;
            }
            String token;
            try {
                token = statisticAvitoService.getToken(account.getClientId(), account.getClientSecret());
            } catch (JsonProcessingException | AvitoResponseException e) {
                log.error(e.getMessage());
                continue;
            }
            Pattern pattern = Pattern.compile("!D[0-9]+");
            Matcher matcher = pattern.matcher(entry.getKey());
            var days = checkLastDate(entry.getKey(), account.getSheetsRef());
            var range = entry.getKey();
            if (matcher.find() || days != 0) {
                try {
                    if (days == 0) {
                        days = 269;
                    } else {
                        var index = range.lastIndexOf('!');
                        var index2 = range.lastIndexOf(':');
                        var rangeWithoutTitleFirst = range.substring(index + 1, index2);
                        var rangeWithoutTitleSecond = range.substring(index2 + 1);
                        StringBuilder letters = new StringBuilder();
                        StringBuilder numbers = new StringBuilder();
                        for (char c : rangeWithoutTitleSecond.toCharArray()) {
                            if (Character.isLetter(c)) {
                                letters.append(c);
                            } else if (Character.isDigit(c)) {
                                numbers.append(c);
                            }
                        }
                        String newLastColumn = letters.toString();
                        for (int i = 0; i < days; i++) {
                            newLastColumn = googleSheetsService.getNextColumn(newLastColumn);
                        }
                        range = name + '!' + rangeWithoutTitleFirst + ':' + newLastColumn + numbers;
                    }
                    days = days == 0 ? 269 : days;
                    var listOldStat = getSummaryStat(account, token, LocalDate.now().minusDays(days));
                    List<List<Object>> all = getStatSummaryMethods().stream()
                            .map(mapper -> listOldStat.stream().
                                    map(mapper)
                                    .collect(Collectors.toList()))
                            .toList();
                    googleSheetsService.insertStatisticIntoTable(all, range, account.getSheetsRef());
                    continue;
                } catch (GoogleSheetsInsertException | GoogleSheetsReadException | AdvertisementServiceException |
                         AvitoResponseException e) {
                    log.error(e.getMessage());
                    continue;
                }
            }
            var list = getYesterdayStats(account, token);
            List<List<Object>> all = getStatSummaryMethods().stream()
                    .map(mapper -> list.stream().
                            map(mapper)
                            .collect(Collectors.toList()))
                    .toList();
            try {
                googleSheetsService.insertStatisticIntoTable(all, entry.getKey(), account.getSheetsRef());
            } catch (GoogleSheetsInsertException e) {
                log.error(e.getMessage());
            }
        }
    }

    private List<StatSummary> getSummaryStat(AccountData account, String token, LocalDate dateFrom) throws GoogleSheetsReadException, AdvertisementServiceException, AvitoResponseException {
        LocalDate oldestDate = dateFrom;
        LocalDate oldDate = dateFrom;
        if (dateFrom.equals(LocalDate.now().minusDays(269))) {
            oldestDate = googleSheetsService.getOldestDate(account.getSheetsRef(), name).orElse(dateFrom);
            oldDate = googleSheetsService.getOldestDate(account.getSheetsRef(), name).orElse(dateFrom);
        }

        Map<LocalDate, Stats> mapStats = new TreeMap<>();
        int i = 0;
        var cost = adsService.findAvgCostAdsByAccountId(account).orElse(BigDecimal.ZERO).doubleValue();
        while (oldestDate.isBefore(LocalDate.now().minusDays(1))) {
            List<Advertisement> allAds = advertisementAvitoService.getAllAdvertisements(token, oldestDate.toString());
            List<Long> listId = allAds.stream()
                    .map(Advertisement::getId)
                    .toList();
            List<Items> itemsWithStats = statisticAvitoService.getStatistic(listId, token, account.getUserId().toString(), oldestDate.toString(), oldestDate.plusDays(29).toString());

            for (Items item : itemsWithStats) {
                var currentItemStats = item.getStats();
                for (Stats stat : currentItemStats) {
                    LocalDate currentDate = LocalDate.parse(stat.getDate());
                    if (mapStats.containsKey(currentDate)) {
                        Stats existingStat = mapStats.get(currentDate);
                        existingStat.setUniqContacts(existingStat.getUniqContacts() + stat.getUniqContacts());
                        existingStat.setUniqFavorites(existingStat.getUniqFavorites() + stat.getUniqFavorites());
                        existingStat.setUniqViews(existingStat.getUniqViews() + stat.getUniqViews());
                        existingStat.setSumViews(existingStat.getSumViews() + stat.getSumViews());
                    } else {
                        mapStats.put(currentDate, stat);
                    }
                }
            }
            oldestDate = oldestDate.plusDays(29);
        }
        System.out.println(i);
        List<Stats> summaryStats = new ArrayList<>(mapStats.values());
        List<Operations> operations = statisticAvitoService.getAmountExpenses(token, oldDate, LocalDate.now());
        List<StatSummary> statsList = new ArrayList<>();
        LocalDate currentDate = oldDate;
        if (dateFrom.equals(LocalDate.now().minusDays(269))) {
            currentDate = SheetsStatUtil.getDayOfStartWeek(oldDate);
        }
        for (Stats stats : summaryStats) {
            if (LocalDate.parse(stats.getDate()).equals(LocalDate.now())) {
                continue;
            }
            operations.stream()
                    .filter(operation -> operation.getUpdatedAt().equals(stats.getDate()))
                    .forEach(operation -> stats.updateSumRaise(operation.getAmountTotal()));
            LocalDate currentDateStats = LocalDate.parse(stats.getDate());
            //if (dateFrom.equals(LocalDate.now().minusDays(269))) {
                statsList.addAll(getMissingDayStats(currentDateStats, currentDate).getKey());
                currentDate = getMissingDayStats(currentDateStats, currentDate).getValue();
            //}

            stats.updateFields(cost);

            LocalDate date = LocalDate.parse(stats.getDate());
            String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, new Locale("ru"));
            statsList.add(new StatSummary(dayOfWeek, stats.getDate(), stats.getUniqViews(),
                    stats.getCv(), stats.getUniqContacts(), stats.getUniqFavorites(),
                    stats.getSumViews(), stats.getSumRaise(), stats.getTotalSum(),
                    stats.getSumContact()));
            if (dayOfWeek.equals("вс")) {
                statsList.addAll(SheetsStatUtil.setStatsWeek(LocalDate.parse(stats.getDate())));
            }
            currentDate = currentDate.plusDays(1);
        }
        statsList.addAll(getMissingDayStats(LocalDate.now().minusDays(1), currentDate.minusDays(1)).getKey());
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

    private List<StatSummary> getYesterdayStats(AccountData account, String token) throws AdvertisementServiceException {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<Advertisement> allAds = advertisementAvitoService.getAllAdvertisements(token, yesterday.toString());
        List<Long> listId = allAds.stream()
                .map(Advertisement::getId)
                .toList();

        var cost = getCost(listId, account);

        List<Items> stats = statisticAvitoService.getStatistic(listId, token, account.getUserId().toString(), yesterday.toString(), yesterday.toString());

        Integer sumV = getSumStatistic(stats, Stats::getUniqViews);
        Integer sumC = getSumStatistic(stats, Stats::getUniqContacts);
        Integer sumF = getSumStatistic(stats, Stats::getUniqFavorites);
        Double allExpenses = getAllExpenses(token, yesterday);

        Stats statistic = new Stats(yesterday.toString(), sumC, sumF, sumV, allExpenses);
        statistic.updateFields(cost);
        List<StatSummary> result = new ArrayList<>();
        result.add(new StatSummary(SheetsStatUtil.getDayOfWeek(yesterday), statistic.getDate(), statistic.getUniqViews(),
                statistic.getCv(), statistic.getUniqContacts(), statistic.getUniqFavorites(), statistic.getSumViews(),
                statistic.getSumRaise(), statistic.getTotalSum(), statistic.getSumContact()));
        if (yesterday.equals("вс")) {
            result.addAll(SheetsStatUtil.setStatsWeek(LocalDate.parse(statistic.getDate())));
        }

        return result;
    }


    private double getAllExpenses(String token, LocalDate date) throws AvitoResponseException {
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

    private double getCost(List<Long> activeAds, AccountData account) {
        var adsFromDB = adsService.findAllAdsByAccountIdAndDate(account, LocalDate.now().minusDays(1));

        Set<Long> adsIdFromAvito = new HashSet<>(activeAds);
        Set<Long> adsFromDb = adsFromDB.stream()
                .map(Ads::getId)
                .collect(Collectors.toSet());

        //1 получить все активные, которых нет в бд
        List<Ads> newAds = new ArrayList<>();
        List<Long> newAdsId = new ArrayList<>(adsIdFromAvito);
        newAdsId.removeAll(adsFromDb.stream().toList());
        for (Long id : newAdsId) {
            newAds.add(Ads.builder()
                    .avitoId(id)
                    .ownerId(account)
                    .pubDate(LocalDate.now().minusDays(1))
                    .closingDate(null)
                    .build());
        }
        parserProcessor.addListAds(newAds);

        //2 получение всех старых и установка их даты

        List<Long> oldAds = new ArrayList<>(adsFromDb);
        oldAds.removeAll(adsIdFromAvito);


        for (Long id : oldAds) {
            adsService.findByAvitoId(id).ifPresent(it -> {
                it.setOwnerId(account);
                it.setClosingDate(LocalDate.now().minusDays(1));
                adsService.save(it);
            });
        }
        return adsService.findAvgCostAdsByAccountIdAndDate(account, LocalDate.now().minusDays(1)).orElse(BigDecimal.ZERO).doubleValue();
    }

    private long checkLastDate(String range, String sheetsRef) {
        var index = range.lastIndexOf('!');
        var index2 = range.lastIndexOf(':');
        var rangeWithoutTitle = range.substring(index + 1, index2);
        StringBuilder letters = new StringBuilder();
        StringBuilder numbers = new StringBuilder();
        for (char c : rangeWithoutTitle.toCharArray()) {
            if (Character.isLetter(c)) {
                letters.append(c);
            } else if (Character.isDigit(c)) {
                numbers.append(c);
            }
        }
        int intRange = Integer.parseInt(numbers.toString()) + 1;
        String letterColumn = getLetterColumn(letters.toString());
        LocalDate yesterday = LocalDate.now().minusDays(1);

        var rangeForCheck = letterColumn + intRange;
        var rangeForCheck2 = name + '!' + rangeForCheck + ':' + rangeForCheck;

            var values = googleSheetsService.getDataFromTable(sheetsRef, rangeForCheck2);
            try {
                var previosDate = LocalDate.parse(values.get(0).get(0).toString());
                if (!previosDate.equals(yesterday.minusDays(1))) {
                    return ChronoUnit.DAYS.between(previosDate, yesterday);
                }
            } catch (DateTimeParseException e) {
                return 0;
            }


        return 0;
    }

    private static String getLetterColumn(String currentColumn) {
        var charArr = currentColumn.toCharArray();
        int length = currentColumn.length();

        for (int i = length - 1; i >= 0; i--) {
            if (charArr[i] == 'A') {
                charArr[i] = 'Z';
            } else {
                charArr[i] = (char) (charArr[i] - 1);
                break;
            }
        }

        if (charArr[0] == 'Z') {
            return new String(charArr, 1, length - 1);
        }

        return new String(charArr);
    }
}
