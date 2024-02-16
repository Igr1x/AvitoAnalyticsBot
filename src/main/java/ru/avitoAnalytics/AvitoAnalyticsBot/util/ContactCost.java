package ru.avitoAnalytics.AvitoAnalyticsBot.util;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.util.List;

public class ContactCost {
    private ContactCost(){};

    public static Double GetCostContact(List<String> searchList) throws Exception {
        String excelFilePath = "classpath:cost.xlsx";
        return findRowByValues(excelFilePath, searchList);
    }

    private static Double findRowByValues(String excelFilePath, List<String> searchList) throws Exception {
        File input = ResourceUtils.getFile(excelFilePath);
        Workbook workbook = WorkbookFactory.create(input);
        Sheet sheet = workbook.getSheetAt(0);
        int bestMatchCount = 0;
        Row bestMatchRow = null;
        for (Row row : sheet) {
            if (!searchList.contains(getDataFromCell(row.getCell(0)))) {
                continue;
            }
            int matchCount = 0;
            for (Cell cell : row) {
                String cellValue = getDataFromCell(cell);
                if (searchList.contains(cellValue)) {
                    matchCount++;
                }
            }
            if (matchCount > bestMatchCount) {
                bestMatchCount = matchCount;
                bestMatchRow = row;
            }
        }
        workbook.close();
        String cost = getDataFromCell(bestMatchRow.getCell(8)).replace(',', '.');
        return Double.parseDouble(cost);
    }

    private static String getDataFromCell(Cell cell) {
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell);
    }
}
