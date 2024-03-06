package ru.avitoAnalytics.AvitoAnalyticsBot.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;

public interface GoogleSheetsService {

    void insertStatisticIntoTable(List<List<Object>> data, String range, String sheetId) throws IOException, GeneralSecurityException;
    void insertTemplateSheets(String sheetsRef);
    boolean checkExistSheets(String sheetsRef);
    List<String> getLinksIdFavouriteItems(String sheetsId);
    List<Long> getIdFavouritesItems(String sheetsLink);
    String getNextColumn(String sheetsLink,String range);
    Map<String, List<Map.Entry<Long, Long>>> getItemsWithRange(String sheetsLink, String range);
}
