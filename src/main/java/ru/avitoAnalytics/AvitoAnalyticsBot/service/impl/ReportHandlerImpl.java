package ru.avitoAnalytics.AvitoAnalyticsBot.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AccountData;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.Ads;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AvitoCost;
import ru.avitoAnalytics.AvitoAnalyticsBot.exceptions.GoogleSheetsReadException;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.*;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.SheetsStatUtil;

import javax.print.attribute.standard.MediaSize;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportHandlerImpl implements ReportHandler {

    private final String GOOGLE_SHEETS_PREFIX = "https://docs.google.com/spreadsheets/d/";
    private static final String RANGE = "%s!A2:G100000";
    private static final String NAME_SHEETS = "HistoryAcc#357725045";

    private final GoogleSheetsService googleSheetsService;
    private final AdsService adsService;
    private final AvitoCostService avitoCostService;
    private final AccountService accountService;

    @Override
    public void reportProcess(String userId) {
        AccountData account = accountService.findByUserId(Long.parseLong(userId)).orElseThrow(() -> {
            log.error("Account with userId {} not found", userId);
            return new RuntimeException();
        });
        //var name = googleSheetsService.getSheetByName(String.format(NAME_SHEETS, userId), account.getSheetsRef());
        try {
            var name = googleSheetsService.getSheetByName(NAME_SHEETS, account.getSheetsRef().substring(GOOGLE_SHEETS_PREFIX.length()).split("/")[0]).get();
            var rang = String.format(RANGE, name);
            var res = googleSheetsService.getDataFromTable(account.getSheetsRef().substring(GOOGLE_SHEETS_PREFIX.length()).split("/")[0], rang);
            if (res.isEmpty()) {
                return;
            }
            for (List<Object> row : res) {
                Ads ad = new Ads();
                ad.setAvitoId(Long.valueOf(row.get(0).toString()));
                ad.setOwnerId(account);
                var cost = adsService.findCostByAvitoId(ad.getAvitoId()).orElse(BigDecimal.ZERO);
                if (cost.equals(BigDecimal.ZERO)) {
                    cost = avitoCostService.findAvitoCost(row.get(1).toString(), row.get(2).toString(), row.get(3).toString(),
                                    row.get(4).toString(), row.get(5).toString(), row.get(6).toString())
                            .orElseGet(AvitoCost::new)
                            .getCost();
                }
                if (cost.equals(BigDecimal.ZERO)) {
                    log.warn("Warning: cost equals zero, ads id - {}", ad.getAvitoId());
                }
                ad.setCost(cost);
                adsService.save(ad);
            }
        } catch (GoogleSheetsReadException e) {
            log.error(e.getMessage());
            log.error(e.getCause().getMessage());
        }
    }
}
