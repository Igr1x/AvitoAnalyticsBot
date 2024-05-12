package ru.avitoAnalytics.AvitoAnalyticsBot.service.impl;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.asynchttpclient.ClientStats;
import org.openqa.selenium.json.JsonOutput;
import org.springframework.stereotype.Service;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AccountData;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.*;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.*;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.SheetsStatUtil;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FullAdsStatisticServiceImpl implements FullAdsStatisticService {

    private final String GOOGLE_SHEETS_PREFIX = "https://docs.google.com/spreadsheets/d/";
    private final String RANGE_FOR_GET_LAST_COLUMN = "%s!A%%d:KI%%d";

    private final AccountService accountService;
    private final GoogleSheetsService googleSheetsService;
    private final StatisticAvitoService statisticAvitoService;
    private final AdvertisementService advertisementService;

    public static String name;

    @Override
    public void setStatistic() {
        List<String> listLinks = accountService.findUniqueSheetsRef();
        if (listLinks.isEmpty()) {
            return;
        }
        updateAccountStats(listLinks);
    }

    private void updateAccountStats(List<String> listSheetsRef) {
        for (String sheetRef : listSheetsRef) {
            String sheetName = googleSheetsService.getSheetByName("StatAcc#", sheetRef.substring(GOOGLE_SHEETS_PREFIX.length()).split("/")[0]).get();
            name = sheetName;
            var sheetWithRangeMap = googleSheetsService.getAccountsWithRange(sheetRef, String.format(RANGE_FOR_GET_LAST_COLUMN, sheetName), sheetName);

            System.out.println();

            try {
                setAccountStats(sheetWithRangeMap);
            } catch (GeneralSecurityException | IOException e) {
                log.error(e.getMessage());
            }
            System.out.println("...");
        }
    }

    private void setAccountStats(Map<String, List<String>> listSheetsRef) throws GeneralSecurityException, IOException {
        for (Map.Entry<String, List<String>> entry : listSheetsRef.entrySet()) {
            AccountData account = accountService.findAllByAccountName(entry.getValue().get(0)).get();
            String token = statisticAvitoService.getToken(account.getClientId(), account.getClientSecret());

            LocalDate yesterday = LocalDate.now().minusDays(1);
            Pattern pattern = Pattern.compile("D[0-9]+");
            Matcher matcher = pattern.matcher(entry.getKey());
            if (matcher.find()) {
                try {
                    var list2 = setHistoryStats(account, entry.getKey(), token);
                    List<List<Object>> all = getStatSummaryMethods().stream()
                            .map(mapper -> list2.stream().
                                    map(mapper)
                                    .collect(Collectors.toList()))
                            .toList();
                    googleSheetsService.insertStatisticIntoTable(all, entry.getKey(), account.getSheetsRef().substring(GOOGLE_SHEETS_PREFIX.length()).split("/")[0]);
                    continue;
                } catch (GeneralSecurityException | IOException e) {
                    log.error(e.getMessage());
                }
            }
            var list = getYesterdayStats(account, token);
            List<List<Object>> all = getStatSummaryMethods().stream()
                    .map(mapper -> list.stream().
                            map(mapper)
                            .collect(Collectors.toList()))
                    .toList();
            googleSheetsService.insertStatisticIntoTable(all, entry.getKey(), account.getSheetsRef().substring(GOOGLE_SHEETS_PREFIX.length()).split("/")[0]);
            System.out.println();
        }
    }

    private List<StatSummary> setHistoryStats(AccountData account, String range, String token) throws GeneralSecurityException, IOException {
        Optional<LocalDate> oldDateOpt = googleSheetsService.getOldestDate(account.getSheetsRef(), name);
        if (oldDateOpt.isEmpty()) {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            LocalDate startWeekDate = SheetsStatUtil.getDayOfStartWeek(yesterday);
            List<StatSummary> startWeekList = new ArrayList<>();
            while (!yesterday.equals(startWeekDate)) {
                startWeekList.add(new StatSummary(SheetsStatUtil.getDayOfWeek(startWeekDate), startWeekDate.toString()));
                startWeekDate = startWeekDate.plusDays(1);
            }
            startWeekList.addAll(getYesterdayStats(account, token));
            return startWeekList;
        }
        LocalDate oldDate = oldDateOpt.get();
        List<StatSummary> allStats = new ArrayList<>();
        while (!oldDate.equals(LocalDate.now().minusDays(1))) {
            allStats.add(new StatSummary(SheetsStatUtil.getDayOfWeek(oldDate), oldDate.toString()));
            if (SheetsStatUtil.getDayOfWeek(oldDate).equals("вс")) {
                allStats.addAll(SheetsStatUtil.setStatsWeek(oldDate));
            }
            oldDate = oldDate.plusDays(1);
        }
        allStats.addAll(getYesterdayStats(account, token));
        return allStats;
    }

    private List<StatSummary> getYesterdayStats(AccountData account, String token) {
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

        Stats statistic = new Stats(yesterday.toString(), sumC, sumF, sumV);
        statistic.updateFields(0.0);
        List<StatSummary> result = new ArrayList<>();
        result.add(new StatSummary(SheetsStatUtil.getDayOfWeek(yesterday), statistic.getDate(), statistic.getUniqViews(),
                statistic.getCv(), statistic.getUniqContacts(), statistic.getUniqFavorites(), statistic.getSumViews(),
                statistic.getSumRaise(), statistic.getTotalSum(), statistic.getSumContact()));
        if (yesterday.equals("вс")) {
            result.addAll(SheetsStatUtil.setStatsWeek(LocalDate.parse(statistic.getDate())));
        }
        return result;
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
