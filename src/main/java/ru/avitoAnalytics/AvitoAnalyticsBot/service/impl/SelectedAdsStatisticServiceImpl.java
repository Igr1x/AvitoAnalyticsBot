package ru.avitoAnalytics.AvitoAnalyticsBot.service.impl;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AccountData;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.Items;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.Stats;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.AccountService;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.GoogleSheetsService;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.SelectedAdsStatisticService;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.StatisticAvitoService;

import java.time.LocalDate;
import java.util.List;

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
            //@TODO
            //добавить поиск цены на просмотры

            List<Long> itemsId = googleSheetsService.getIdFavouritesItems(links);
//
            System.out.println(getRange(account.getSheetsRef(), 1));

            //String token = statisticAvitoService.getToken(account.getClientId(), account.getClientSecret());
            //List<Items> itemsList = statisticAvitoService.getStatistic(itemsId, token, account.getUserId(),  );
        }

        for (int i = 0; i < listAccounts.size(); i++) {
            AccountData account = listAccounts.get(i);
            List<String> links = googleSheetsService.getLinksIdFavouriteItems(account.getSheetsRef());
            List<Long> itemsId = googleSheetsService.getIdFavouritesItems(links);
            String range = getRange(account.getSheetsRef(), i + 1);
            LocalDate dateTo = LocalDate.now().minusDays(1);
            LocalDate dateFrom = dateTo;
            if (range.startsWith("test!D")) {
                dateFrom = dateTo.minusDays(269);
            }
            String token = statisticAvitoService.getToken(account.getClientId(), account.getClientSecret());
            List<Items> listStats = statisticAvitoService.getStatistic(itemsId, token, account.getUserId().toString(), dateFrom.toString(), dateTo.toString());
            /*for (Items item : listStats) {
                List<Stats> stats = item.getStats();
                for (Stats stat : stats) {

                }
            }*/
        }
    }

    private List<AccountData> getAllAccounts() {
        return accountService.findAll();
    }

    private String getRange(String sheetsLink, int i) {
        String nextColumn = googleSheetsService.getNextColumn(sheetsLink, String.format(RANGE_FOR_GET_LAST_COLUMN, ++i, i));
        if (nextColumn.equals("D")) {
            return "test!D%d:JM%d";
        }
        StringBuilder range = new StringBuilder("test!");
        range.append(nextColumn).append("%d:").append(nextColumn).append("%d");
        return range.toString();
    }
}
