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
import ru.avitoAnalytics.AvitoAnalyticsBot.models.AvitoItems;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.GoogleSheetsService;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GoogleSheetsServiceImpl implements GoogleSheetsService {

    private static final String APPLICATION_NAME = "Google Sheets Example";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String FROM_SHEETS_ID = "1WJ-Url5L6obzKMUtCUxkk3KKZYeV7NmSuCRP6MAWQR4";
    private static final String PREFIX_SHEETS_REF = "https://docs.google.com/spreadsheets/d/";
    private static final String ADS_ID_RANGE = "%s!B%%d:B%%d";
    private static final String OLDEST_DATE_ADS_RANGE = "%s!D3:D3";

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
            log.error("Error creating the table server");
        }
    }

    @Override
    public void insertStatisticIntoTable(List<List<Object>> data, String range, String sheetId) throws IOException {
        ValueRange body = new ValueRange().setValues(data);
        service.spreadsheets().values().update(sheetId, range, body)
                .setValueInputOption("USER_ENTERED")
                .execute();
    }

    @Override
    public void insertTemplateSheets(String sheetsRef) {
        try {
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
            setSheetsTitle(toSheetsId);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
            return false;
        }
    }

    @Override
    public List<String> getLinksIdFavouriteItems(String sheetsLink, String sheetTittle) {
        String sheetsId = parseTokenFromSheetsRef(sheetsLink);
        List<String> itemsId = new ArrayList<>();
        String range = String.format(ADS_ID_RANGE, sheetTittle);
        ValueRange value;
        for (int i = 0; ; i++) {
            int currentRange = (i * 15) + 2;
            try {
                value = service.spreadsheets().values().get(sheetsId, String.format(range, currentRange, currentRange)).execute();
                if (value.getValues() == null) {
                    break;
                }
                itemsId.add((String) value.getValues().get(0).get(0));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return itemsId;
    }

    @Override
    public List<Long> getIdFavouritesItems(List<String> itemsId) {
        return itemsId.stream()
                .map(s -> s.substring(s.lastIndexOf('_') + 1))
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    @Override
    public String getNextColumn(String sheetsLink, String range) {
        String column = getColumnLetter(sheetsLink, range);
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
    public Map<String, List<AvitoItems>> getItemsWithRange(String sheetsLink, String range, String sheetTittle) {
        List<AvitoItems> listItems = getItemsWithLink(sheetsLink, sheetTittle);
        Map<String, List<AvitoItems>> itemsRange = new HashMap<>();
        int i = 0;
        for (AvitoItems item : listItems) {
            int paramForRange = (i * 15) + 3;
            String currentItemRange = String.format(range, paramForRange, paramForRange);
            String nextColumn = getNextColumn(sheetsLink, currentItemRange);
            String dopColumn = getDopColumn(nextColumn);
            String newRange = createRange(nextColumn, dopColumn, sheetTittle);
            List<AvitoItems> itemList = itemsRange.computeIfAbsent(newRange, k -> new ArrayList<>());
            itemList.add(item);
            i++;
        }
        return itemsRange;
    }

    @Override
    public Optional<LocalDate> getOldestDate(String sheetsLink, String sheetTittle) {
        String range = String.format(OLDEST_DATE_ADS_RANGE, sheetTittle);
        String sheetsId = parseTokenFromSheetsRef(sheetsLink);
        try {
            ValueRange value = service.spreadsheets().values().get(sheetsId, range).execute();
            if (value.getValues() != null) {
                return Optional.of(LocalDate.parse(value.getValues().get(0).get(0).toString()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    private String getDopColumn(String column) {
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

    private List<AvitoItems> getItemsWithLink(String sheetsLink, String sheetTittle) {
        List<String> itemsId = getLinksIdFavouriteItems(sheetsLink, sheetTittle);
        List<Long> itemsLongId = getIdFavouritesItems(itemsId);
        List<AvitoItems> result = new ArrayList<>();
        for (int i = 0; i < itemsLongId.size(); i++) {
            result.add(new AvitoItems((long) i, itemsLongId.get(i), itemsId.get(i)));
        }
        return result;
    }

    private String createRange(String nextColumn, String dopColumn, String sheetTittle) {
        if (nextColumn.equals("C")) {
            String range = "%s!D%%d:RH%%d";
            return String.format(range, sheetTittle);
        }
        StringBuilder range = new StringBuilder(sheetTittle);
        range.append("!").append(nextColumn).append("%d:").append(dopColumn).append("%d");
        return range.toString();
    }

    private String getColumnLetter(String sheetsLink, String range) {
        int columnNumber = getLastColumnNumber(sheetsLink, range);
        StringBuilder column = new StringBuilder();
        while (columnNumber > 0) {
            column.insert(0, (char) ('A' + (columnNumber - 1) % 26));
            columnNumber = (columnNumber - 1) / 26;
        }
        return column.toString();
    }

    private int getLastColumnNumber(String sheetsLink, String range) {
        String sheetsId = parseTokenFromSheetsRef(sheetsLink);
        int lastColumn = 0;
        try {
            ValueRange values = service.spreadsheets().values().get(sheetsId, range).execute();
            if (!values.isEmpty()) {
                lastColumn = values.getValues().get(0).size();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return lastColumn;
    }


    private void setSheetsTitle(String sheetsId) throws IOException {
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
            service.spreadsheets().batchUpdate(sheetsId, body).execute();
        }
    }

    private String deleteSubstringFromTitleSheet(String titleSheet, String substring) {
        return titleSheet.replace(substring, "").trim();
    }

    private String parseTokenFromSheetsRef(String sheetsRef) {
        return sheetsRef.substring(PREFIX_SHEETS_REF.length()).split("/")[0];
    }
}
