package ru.avitoAnalytics.AvitoAnalyticsBot.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;
import org.springframework.stereotype.Service;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.GoogleSheetsService;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleSheetsServiceImpl implements GoogleSheetsService {

    private static String APPLICATION_NAME = "Google Sheets Example";
    private static JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    @Override
    public void insertStatisticIntoTable(List<List<Object>> data, String range, String sheetId) throws IOException, GeneralSecurityException {
        //String range = "Избранные!D2:AP10";
        //String id = "1WJ-Url5L6obzKMUtCUxkk3KKZYeV7NmSuCRP6MAWQR4";
        Sheets service = getSheetsService();
        ValueRange body = new ValueRange().setValues(data);
        service.spreadsheets().values().update(sheetId, range, body)
                .setValueInputOption("USER_ENTERED")
                .execute();
    }

    private void setBackgroundColor(Sheets service, String id) throws IOException {
        List<Request> requests = new ArrayList<>();
        for (int i = 3; i <= 6; i++) {
            String range = "D" + i + ":AP" + i;
            String conditionFormula = "=$C" + i + ">D" + i;
            // Создание условного форматирования для текущей строки
            ConditionalFormatRule rule = new ConditionalFormatRule()
                    .setRanges(Collections.singletonList(new GridRange().setSheetId(0).setStartRowIndex(i - 1).setEndRowIndex(i).setStartColumnIndex(1).setEndColumnIndex(5)))
                    .setBooleanRule(new BooleanRule()
                            .setCondition(new BooleanCondition().setType("CUSTOM_FORMULA").setValues(Collections.singletonList(new ConditionValue().setUserEnteredValue(conditionFormula))))
                            .setFormat(new CellFormat().setBackgroundColor(
                                    new Color().setRed(1f)
                            )));
            requests.add(new Request().setAddConditionalFormatRule(new AddConditionalFormatRuleRequest().setRule(rule).setIndex(i - 1)));
        }
        BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest().setRequests(requests);
        service.spreadsheets().batchUpdate(id, batchUpdateRequest).execute();
    }

    private Sheets getSheetsService() throws IOException, GeneralSecurityException {
        GoogleCredential credential = getCredential();
        return new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private GoogleCredential getCredential() throws IOException {
        InputStream io = this.getClass()
                .getClassLoader()
                .getResourceAsStream("creds.json");
        return GoogleCredential.fromStream(io)
                .createScoped(Collections.singletonList(SheetsScopes.SPREADSHEETS));
    }
}
