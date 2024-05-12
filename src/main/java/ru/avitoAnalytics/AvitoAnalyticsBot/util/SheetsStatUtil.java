package ru.avitoAnalytics.AvitoAnalyticsBot.util;

import lombok.experimental.UtilityClass;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.StatSummary;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@UtilityClass
public class SheetsStatUtil {

    public static List<StatSummary> setStatsWeek(LocalDate lastDate) {
        String templateForStats = "=SUM(OFFSET(INDIRECT(ADDRESS(ROW();COLUMN();));0;-7;1;7))";
        String templateForCV = "=CELL(\"contents\"; INDIRECT(ADDRESS(ROW()+1;COLUMN();))) / CELL(\"contents\"; INDIRECT(ADDRESS(ROW()-1;COLUMN();)))";
        String startWeek = lastDate.minusDays(6).toString();
        return List.of(new StatSummary("Итог недели", startWeek + "-" + lastDate.toString(),
                templateForStats,
                templateForCV,
                templateForStats,
                templateForStats,
                templateForStats,
                templateForStats,
                templateForStats,
                templateForStats));
    }

    public static String getDayOfWeek(LocalDate date) {
        return date.getDayOfWeek().getDisplayName(TextStyle.SHORT, new Locale("ru"));
    }

    public static LocalDate getDayOfStartWeek(LocalDate date) {
        LocalDate newDate = date;
        while (!SheetsStatUtil.getDayOfWeek(newDate).equals("пн")) {
            newDate = newDate.minusDays(1);
        }
        return newDate;
    }

}
