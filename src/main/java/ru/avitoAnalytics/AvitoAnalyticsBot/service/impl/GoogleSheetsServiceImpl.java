package ru.avitoAnalytics.AvitoAnalyticsBot.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.Sheets.Spreadsheets.SheetsOperations.CopyTo;
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

    private static final String APPLICATION_NAME = "Google Sheets Example";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String FROM_SHEETS_ID = "1WJ-Url5L6obzKMUtCUxkk3KKZYeV7NmSuCRP6MAWQR4";
    private static final String PREFIX_SHEETS_REF = "https://docs.google.com/spreadsheets/d/";

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

    @Override
    public void insertTemplateSheets(String sheetsRef) throws IOException, GeneralSecurityException {
        GoogleCredential credential = getCredential();
        Sheets service = new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        Spreadsheet spreadsheet = service.spreadsheets().get(FROM_SHEETS_ID).execute();
        List<Sheet> sheets = spreadsheet.getSheets();

        List<Integer> sheetsId = new ArrayList<>();
        for (Sheet sheet : sheets) {
            sheetsId.add(sheet.getProperties().getSheetId());
        }

        String toSheetsId = parseTokenFromSheetsRef(sheetsRef);

        CopySheetToAnotherSpreadsheetRequest requestBody = new CopySheetToAnotherSpreadsheetRequest();
        requestBody.setDestinationSpreadsheetId(toSheetsId);
        for (Integer sheetId : sheetsId) {
            CopyTo request = service.spreadsheets().sheets().copyTo(FROM_SHEETS_ID, sheetId, requestBody);
            request.execute();
        }
        setSheetsTitle(service, toSheetsId);
    }

    private void setSheetsTitle(Sheets service, String sheetsId) throws IOException {
        Spreadsheet spreadsheet = service.spreadsheets().get(sheetsId).execute();
        List<Sheet> sheets = spreadsheet.getSheets();

        for (Sheet sheet : sheets) {
            SheetProperties properties = sheet.getProperties();
            String titleSheet = deleteSubstringFromTitleSheet(properties.getTitle(), "(копия)");
            Integer sheetId = properties.getSheetId();
            List<Request> requests = new ArrayList<>();
            Request request = new Request()
                    .setUpdateSheetProperties(new UpdateSheetPropertiesRequest()
                            .setProperties(new SheetProperties()
                                    .setSheetId(sheetId)
                                    .setTitle(titleSheet))
                            .setFields("title"));
            requests.add(request);
            BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest().setRequests(requests);
            BatchUpdateSpreadsheetResponse response = service.spreadsheets().batchUpdate(sheetsId, body).execute();
        }
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

    private String deleteSubstringFromTitleSheet(String titleSheet, String substring) {
        return titleSheet.replace(substring, "").trim();
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

    private String parseTokenFromSheetsRef(String sheetsRef) {
        return sheetsRef.substring(PREFIX_SHEETS_REF.length()).split("/")[0];
    }

}
