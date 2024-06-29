package ru.avitoAnalytics.AvitoAnalyticsBot.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AccountData;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.Ads;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AvitoCost;
import ru.avitoAnalytics.AvitoAnalyticsBot.exceptions.GoogleSheetsReadException;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportHandlerImpl implements ReportHandler {

    private static final String RANGE = "%s!A2:J100000";
    private static final String NAME_SHEETS = "HistoryAcc#%s";

    private final GoogleSheetsService googleSheetsService;
    private final AdsService adsService;
    private final AvitoCostService avitoCostService;
    private final AccountServiceImpl accountService;

    @Override
    public void reportProcess(AccountData account) {
        try {
            account.setReport(true);
            accountService.saveAccount(account);
            var name = googleSheetsService.getSheetByName(String.format(NAME_SHEETS, account.getUserId()), account.getSheetsRef()).orElseThrow(() -> new GoogleSheetsReadException("Sheet not found"));
            var rang = String.format(RANGE, name);
            var res = googleSheetsService.getDataFromTable(account.getSheetsRef(), rang);
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
                if (cost == null || cost.equals(BigDecimal.ZERO)) {
                    log.warn("Warning: cost equals zero, ads id - {}", ad.getAvitoId());
                    continue;
                }
                ad.setPubDate(LocalDate.parse(row.get(8).toString()));
                ad.setClosingDate(LocalDate.parse(row.get(9).toString()));
                ad.setCost(cost);
                adsService.save(ad);
            }
        } catch (Exception e) {
            account.setReport(false);
            accountService.saveAccount(account);
            log.error(e.getMessage(), e);
        }
    }
}
