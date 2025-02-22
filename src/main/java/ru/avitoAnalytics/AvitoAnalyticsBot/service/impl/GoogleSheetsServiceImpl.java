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
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.AvitoItems;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.StatSummary;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.GoogleSheetsService;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GoogleSheetsServiceImpl implements GoogleSheetsService {

    private static final String APPLICATION_NAME = "Google Sheets Example";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String FROM_SHEETS_ID = "1WJ-Url5L6obzKMUtCUxkk3KKZYeV7NmSuCRP6MAWQR4";
    private static final String PREFIX_SHEETS_REF = "https://docs.google.com/spreadsheets/d/";

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
    public List<String> getLinksIdFavouriteItems(String sheetsLink) {
        String sheetsId = parseTokenFromSheetsRef(sheetsLink);
        List<String> itemsId = new ArrayList<>();
        String range = "test!B%d:B%d";
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
        try {
            Thread.sleep(60000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
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
    public Map<String, List<AvitoItems>> getItemsWithRange(String sheetsLink, String range) {
        List<AvitoItems> listItems = getItemsWithLink(sheetsLink);
        Map<String, List<AvitoItems>> itemsRange = new HashMap<>();
        int i = 0;
        for (AvitoItems item : listItems) {
            int paramForRange = (i * 15) + 2;
            String currentItemRange = String.format(range, paramForRange, paramForRange);
            String nextColumn = getNextColumn(sheetsLink, currentItemRange);
            String dopColumn = getDopColumn(nextColumn);
            String newRange = createRange(nextColumn, dopColumn);
            List<AvitoItems> itemList = itemsRange.computeIfAbsent(newRange, k -> new ArrayList<>());
            itemList.add(item);
            i++;
        }
        return itemsRange;
    }

    @Override
    public Optional<LocalDate> getOldestDate(String sheetsLink) {
        String range = "test!D2:D2";
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

    /*@Override
    public long getLastWeekStatistic(String sheetsLink, String currentRange) {
        String sheetsId = parseTokenFromSheetsRef(sheetsLink);
        String range = getPatterRange(calculateInterval(currentRange)).getKey();
        Long id = Long.parseLong(getPatterRange(calculateInterval(currentRange)).getValue());
        long sum = 0;
        try {
            ValueRange valueViews = service.spreadsheets().values().get(sheetsId, "test!" + String.format(range, id + 2, id + 2)).execute();
            List<List<Object>> values = valueViews.getValues();
            for (List<Object> row : values) {
                for (Object value : row) {
                    long longValue = Long.parseLong(value.toString());
                    sum += longValue;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sum;
    }

    private static Pair<String, String> getPatterRange(String range) {
        String regex = "(\\d+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(range);

        String number = matcher.group(1);
        String modifiedString = range.replaceAll(regex, "%d");
        return Pair.of(modifiedString, number);

    }

    private static String calculateInterval(String s) {
        String partAfterExclamation = s.split("!")[1];
        String firstPart = partAfterExclamation.split(":")[0];
        String lettersPart = firstPart.replaceAll("[0-9]", "");
        String numbersPart = firstPart.substring(lettersPart.length());
        int lettersNumber = columnLettersToNumber(lettersPart) - 6;
        String newLettersPart = columnNumberToLetters(lettersNumber);


        return newLettersPart + numbersPart + ":" + lettersPart + numbersPart;
    }

    private static int columnLettersToNumber(String letters) {
        int number = 0;
        for (int i = 0; i < letters.length(); i++) {
            number *= 26;
            number += letters.charAt(i) - 'A' + 1;
        }
        return number;
    }

    private static String columnNumberToLetters(int number) {
        StringBuilder letters = new StringBuilder();
        while (number > 0) {
            number--;
            char letter = (char) ('A' + (number % 26));
            letters.insert(0, letter);
            number /= 26;
        }
        return letters.toString();
    }*/

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

    private List<AvitoItems> getItemsWithLink(String sheetsLink) {
        List<String> itemsId = getLinksIdFavouriteItems(sheetsLink);
        List<Long> itemsLongId = getIdFavouritesItems(itemsId);
        List<AvitoItems> result = new ArrayList<>();
        for (int i = 0; i < itemsLongId.size(); i++) {
            result.add(new AvitoItems((long) i, itemsLongId.get(i), itemsId.get(i)));
        }
        return result;
    }

    private String createRange(String nextColumn, String dopColumn) {
        if (nextColumn.equals("C")) {
            return "test!D%d:JM%d";
        }
        StringBuilder range = new StringBuilder("test!");
        range.append(nextColumn).append("%d:").append(dopColumn).append("%d");
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

    private void setBackgroundColor(String id) throws IOException {
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

    private String parseTokenFromSheetsRef(String sheetsRef) {
        return sheetsRef.substring(PREFIX_SHEETS_REF.length()).split("/")[0];
    }
}
