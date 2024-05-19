package ru.avitoAnalytics.AvitoAnalyticsBot.service;

import com.google.api.services.sheets.v4.model.Sheet;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.AvitoItems;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.StatSummary;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface GoogleSheetsService {

    void insertStatisticIntoTable(List<List<Object>> data, String range, String sheetId) throws IOException, GeneralSecurityException;
    void insertTemplateSheets(String sheetsRef);
    boolean checkExistSheets(String sheetsRef);
    Map<String, String> getLinksIdFavouriteItems(String sheetsId, String sheetTittle, int p1, int p2);
    //List<Long> getIdFavouritesItems(List<String> itemsId);
    Map<Long, String> getIdFavouritesItems(Map<String, String> itemsId);
    String getNextColumn(String column);
    Map<String, List<AvitoItems>> getItemsWithRange(String sheetsLink, String range, String sheetTittle, Integer param1, Integer param2);
    Optional<LocalDate> getOldestDate(String sheetsLink, String sheetTittle);
    Optional<String> getSheetByName(String nameSheets, String sheetsRef);
    Map<String, List<String>> getAccountsWithRange(String sheetsLink, String range, String sheetTittle);
}
