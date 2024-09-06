package ru.avitoAnalytics.AvitoAnalyticsBot.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.Sheets.Spreadsheets.SheetsOperations.CopyTo;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.avitoAnalytics.AvitoAnalyticsBot.exceptions.GoogleSheetsInsertException;
import ru.avitoAnalytics.AvitoAnalyticsBot.exceptions.GoogleSheetsReadException;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.AvitoItems;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.GoogleSheetsService;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GoogleSheetsServiceImpl implements GoogleSheetsService {

    private static final String APPLICATION_NAME = "Google Sheets Example";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String FROM_SHEETS_ID = "1c_VR7A-mnR7rpJ6dfcRBnjJGIIX2vIsxiZ2F2TcUrP4";
    private static final String PREFIX_SHEETS_REF = "https://docs.google.com/spreadsheets/d/";
    private static final String ADS_ID_RANGE = "%s!B%%d:C%%d";
    private static final String OLDEST_DATE_ADS_RANGE = "%s!D2:D2";
    private static final String AVITO = "https://avito.ru/%d";

    private static Sheets service;

    static {
        try (InputStream io = GoogleSheetsServiceImpl.class
                .getClassLoader()
                .getResourceAsStream("creds.json")) {
            GoogleCredential credential = GoogleCredential.fromStream(Objects.requireNonNull(io))
                    .createScoped(Collections.singletonList(SheetsScopes.SPREADSHEETS));
            service = new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (IOException | GeneralSecurityException e) {
            log.error("Error: creating the table server, message: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<List<Object>> getDataFromTable(String sheetId, String range) {
        try {
            return service.spreadsheets().values().get(sheetId, range).execute().getValues();
        } catch (IOException e) {
            throw new GoogleSheetsReadException(String.format("Error: get data from table %s", sheetId), e);
        }
    }

    @Override
    public void insertStatisticIntoTable(List<List<Object>> data, String range, String sheetId) {
        ValueRange body = new ValueRange().setValues(data);
        try {
            service.spreadsheets().values().update(sheetId, range, body)
                    .setValueInputOption("USER_ENTERED")
                    .execute();
        } catch (IOException e) {
            throw new GoogleSheetsInsertException(String.format("Error: inserting data into table %s", sheetId), e);
        }
    }

    @Override
    public void insertTemplateSheets(String toSheetsId) throws GoogleSheetsReadException, GoogleSheetsInsertException {
        try {
            List<Integer> sheetsId = getSheetsId(FROM_SHEETS_ID);

            CopySheetToAnotherSpreadsheetRequest requestBody = new CopySheetToAnotherSpreadsheetRequest();
            requestBody.setDestinationSpreadsheetId(parseTokenFromSheetsRef(toSheetsId));

            for (Integer sheetId : sheetsId) {
                CopyTo request = service.spreadsheets().sheets().copyTo(FROM_SHEETS_ID, sheetId, requestBody);
                request.execute();
            }
            setSheetsTitle(toSheetsId);
        } catch (IOException e) {
            throw new GoogleSheetsInsertException(String.format("Error: inserting new sheets into %s", toSheetsId), e);
        }
    }

    private List<Sheet> getSheetsFromSpreadsheets(String sheetsRef) {
        try {
            Spreadsheet spreadsheet = service.spreadsheets().get(sheetsRef).execute();
            return spreadsheet.getSheets();
        } catch (IOException e) {
            throw new GoogleSheetsReadException(String.format("Error: get all sheets from sheets %s", sheetsRef), e);
        }
    }

    private List<Integer> getSheetsId(String sheetsRef) throws GoogleSheetsReadException {
        List<Sheet> sheets = getSheetsFromSpreadsheets(sheetsRef);

        List<Integer> sheetsId = new ArrayList<>();
        for (Sheet sheet : sheets) {
            sheetsId.add(sheet.getProperties().getSheetId());
        }
        return sheetsId;
    }

    private void setSheetsTitle(String sheetsId) throws GoogleSheetsReadException {
        try {
            List<Sheet> sheets = getSheetsFromSpreadsheets(sheetsId);

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
                service.spreadsheets().batchUpdate(sheetsId, body).execute();
            }
        } catch (IOException e) {
            throw new GoogleSheetsInsertException(String.format("Error: set new sheets title into %s", sheetsId), e);
        }
    }

    @Override
    public boolean checkExistSheets(String sheetsRef) {
        try {
            if (!sheetsRef.startsWith(PREFIX_SHEETS_REF)) return false;
            String sheetsId = parseTokenFromSheetsRef(sheetsRef);
            Spreadsheet spreadsheet = service.spreadsheets().get(sheetsId).execute();
            return spreadsheet != null;
        } catch (IOException ex) {
            throw new GoogleSheetsReadException(String.format("Error: check exist sheets %s", sheetsRef), ex);
        }
    }

    @Override
    public Map<String, String> getLinksIdFavouriteItems(String sheetsLink, String sheetTittle, int p1, int p2) {
        Map<String, String> itemsIdWithAccount = new LinkedHashMap<>();
        String range = String.format(ADS_ID_RANGE, sheetTittle);
        ValueRange value;
        for (int i = 0; ; i++) {
            int currentRange = (i * p1) + p2;
            try {
                value = service.spreadsheets().values().get(sheetsLink, String.format(range, currentRange, currentRange)).execute();
                if (value.getValues() == null || value.getValues().isEmpty() || value.getValues().get(0) == null) {
                    break;
                }
                String itemId = value.getValues().get(0).get(0).toString();
                if (itemId.equals("Просмотров") || itemId.equals("Аккаунт ХХХ")) {
                    break;
                }

                String accountData = "unknown";
                if (value.getValues().get(0).size() == 2) {
                    accountData = value.getValues().get(0).get(1).toString();
                }
                itemsIdWithAccount.put(itemId, accountData);
                Thread.currentThread().sleep(5L);
            } catch (IOException e) {
                throw new GoogleSheetsReadException(String.format("Error: read account name from %s", sheetsLink));
            } catch (InterruptedException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
        return itemsIdWithAccount;
    }

    @Override
    public Map<Long, String> getIdFavouritesItems(Map<String, String> itemsId) {
        return itemsId.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> Long.parseLong(entry.getKey().substring(entry.getKey().lastIndexOf('_') + 1)),
                        Map.Entry::getValue,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }

    @Override
    public String getNextColumn(String column) {
        char[] chars = column.toCharArray();
        int length = chars.length;
        int index = length - 1;

        while (index >= 0) {
            if (chars[index] == 'Z') {
                chars[index] = 'A';
                index--;
            } else {
                chars[index]++;
                return new String(chars);
            }
        }
        return "A" + new String(chars);
    }

    @Override
    public Map<String, List<AvitoItems>> getItemsWithRange(String sheetsLink, String range, String sheetTittle, Integer param1, Integer param2) throws GoogleSheetsReadException {
        List<AvitoItems> listItems = getItemsWithLink(sheetsLink, sheetTittle);
        Map<String, List<AvitoItems>> itemsRange = new HashMap<>();
        int i = 0;
        for (AvitoItems item : listItems) {
            int paramForRange = (i * param1) + param2;
            String currentItemRange = String.format(range, paramForRange, paramForRange);
            int lastColumnNumber = getLastColumnNumber(sheetsLink, currentItemRange);
            String currentColumn = getColumnLetter(lastColumnNumber);
            String nextColumn = getNextColumn(currentColumn);
            String dopColumn = getDopColumn(nextColumn);
            String newRange = createRange(nextColumn, dopColumn, sheetTittle);
            List<AvitoItems> itemList = itemsRange.computeIfAbsent(newRange, k -> new ArrayList<>());
            itemList.add(item);
            i++;
        }
        return itemsRange;
    }

    private List<AvitoItems> getItemsWithLink(String sheetsLink, String sheetTittle) {
        Map<String, String> itemsId = getLinksIdFavouriteItems(sheetsLink, sheetTittle, 15, 2);
        Map<Long, String> itemsLongId = getIdFavouritesItems(itemsId);
        List<AvitoItems> result = new ArrayList<>();
        int i = 0;
        for (Map.Entry<Long, String> entry : itemsLongId.entrySet()) {
            result.add(new AvitoItems((long) i, entry.getKey(), String.format(AVITO, entry.getKey()), entry.getValue()));
            i++;
        }
        return result;
    }

    @Override
    public Map<String, List<String>> getAccountsWithRange(String sheetsLink, String range, String sheetTittle) throws GoogleSheetsReadException {
        Map<String, String> accountLinks = getLinksIdFavouriteItems(sheetsLink, sheetTittle, 12, 2);
        Map<String, List<String>> itemsRange = new HashMap<>();
        int i = 0;
        for (Map.Entry<String, String> entry : accountLinks.entrySet()) {
            int paramForRange = (i * 12) + 2;
            String currentItemRange = String.format(range, paramForRange, paramForRange);
            int lastColumnNumber = getLastColumnNumber(sheetsLink, currentItemRange);
            String currentColumn = getColumnLetter(lastColumnNumber);
            String nextColumn = getNextColumn(currentColumn);
            String dopColumn = getDopColumn(nextColumn);
            String newRange = String.format(createRange(nextColumn, dopColumn, sheetTittle), (i * 12) + 1, (i * 12) + 11);
            List<String> itemList = itemsRange.computeIfAbsent(newRange, k -> new ArrayList<>());
            itemList.add(entry.getKey());
            i++;
        }
        return itemsRange;
    }

    private String createRange(String nextColumn, String dopColumn, String sheetTittle) {
        if (nextColumn.equals("D")) {
            String range = "%s!D%%d:RH%%d";
            return String.format(range, sheetTittle);
        }
        StringBuilder range = new StringBuilder(sheetTittle);
        range.append("!").append(nextColumn).append("%d:").append(dopColumn).append("%d");
        return range.toString();
    }

    private String getDopColumn(String column) {
        char[] chars = column.toCharArray();
        int length = chars.length;
        int index = length - 1;

        while (index >= 0) {
            if (chars[index] == 'Z') {
                chars[index] = 'B';
                index--;
                if (index < 0) {
                    return "AB";
                }
            } else if (chars[index] == 'Y') {
                chars[index] = 'A';
                return "B" + new String(chars);
            } else {
                chars[index] += 2;
                return new String(chars);
            }
        }
        return "B" + new String(chars);
    }

    @Override
    public Optional<LocalDate> getOldestDate(String sheetsId, String sheetTittle) {
        String range = String.format(OLDEST_DATE_ADS_RANGE, sheetTittle);
        try {
            ValueRange value = service.spreadsheets().values().get(sheetsId, range).execute();
            if (value.getValues() != null) {
                return Optional.of(LocalDate.parse(value.getValues().get(0).get(0).toString()));
            }
            return Optional.empty();
        } catch (IOException | DateTimeParseException e) {
            throw new GoogleSheetsReadException(String.format("Error: read oldest date from %s", sheetsId), e);
        }
    }

    @Override
    public Optional<String> getSheetByName(String nameSheets, String sheetsRef) {
        try {
            Spreadsheet spreadsheet = service.spreadsheets().get(sheetsRef).execute();
            for (Sheet sheet : spreadsheet.getSheets()) {
                if (sheet.getProperties().getTitle().contains(nameSheets)) {
                    return Optional.of(sheet.getProperties().getTitle());
                }
            }
            return Optional.empty();
        } catch (IOException e) {
            throw new GoogleSheetsReadException(String.format("Error: read by sheets name %s from %s", nameSheets, sheetsRef), e);
        }
    }

    private String getColumnLetter(int columnNumber) {
        StringBuilder column = new StringBuilder();
        while (columnNumber > 0) {
            column.insert(0, (char) ('A' + (columnNumber - 1) % 26));
            columnNumber = (columnNumber - 1) / 26;
        }
        return column.toString();
    }

    private int getLastColumnNumber(String sheetsId, String range) {
        int lastColumn = 0;
        try {
            ValueRange values = service.spreadsheets().values().get(sheetsId, range).execute();
            Thread.sleep(5L);
            if (!values.isEmpty()) {
                lastColumn = values.getValues().get(0).size();
            }
            return lastColumn;
        } catch (IOException e) {
            throw new GoogleSheetsReadException(String.format("Error: get last column from %s, range %s", sheetsId, range), e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String deleteSubstringFromTitleSheet(String titleSheet, String substring) {
        return titleSheet.replace(substring, "").trim();
    }

    private String parseTokenFromSheetsRef(String sheetsRef) {
        return sheetsRef.substring(PREFIX_SHEETS_REF.length()).split("/")[0];
    }
}
