package ru.avitoAnalytics.AvitoAnalyticsBot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AccountData;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.AccountService;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.AdsService;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.GoogleSheetsService;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.ReportHandler;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.SheetsStatUtil;

import javax.print.attribute.standard.MediaSize;

@Service
@RequiredArgsConstructor
public class ReportHandlerImpl implements ReportHandler {

    private static final String RANGE = "%s!A100000:G100000";
    private static final String NAME_SHEETS = "HistoryAcc#%s";

    private final GoogleSheetsService googleSheetsService;
    private final AdsService adsService;
    private final AccountService accountService;


    @Override
    public void reportProcess(String userId) {
        AccountData account = accountService.findByUserId(Long.parseLong(userId)).orElseThrow(() -> new RuntimeException("Account not found"));
        var name = googleSheetsService.getSheetByName(String.format(NAME_SHEETS, userId), account.getSheetsRef());
        var res = googleSheetsService.getDataFromTable(String.format(RANGE, name), account.getSheetsRef());
        if (res.isEmpty()) {
            return;
        }



    }
}
