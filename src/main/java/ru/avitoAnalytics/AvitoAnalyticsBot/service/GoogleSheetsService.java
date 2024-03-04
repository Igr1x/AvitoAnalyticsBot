package ru.avitoAnalytics.AvitoAnalyticsBot.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public interface GoogleSheetsService {

    void insertStatisticIntoTable(List<List<Object>> data, String range, String sheetId) throws IOException, GeneralSecurityException;
    void insertTemplateSheets(String sheetsRef);
    boolean checkExistSheets(String sheetsRef);
    List<String> getLinksIdFavouriteItems(String sheetsId);
    List<Long> getIdFavouritesItems(List<String> itemsId);
    String getNextColumn(String sheetsLink,String range);
}
