package ru.avitoAnalytics.AvitoAnalyticsBot.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class AvitoParser {
    private AvitoParser() {};

    public static List<String> getDataForTable(String link) throws IOException, GeneralSecurityException, InterruptedException {
        Document doc = Jsoup.connect(link).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 YaBrowser/24.1.0.0 Safari/537.36").get();
        List<String> forTable = new ArrayList<>(List.of("другой регион"));
        forTable.addAll(getAddress(doc));
        forTable.addAll(getHeaders(doc));
        forTable.add(getState(doc));
        forTable.forEach(System.out::println);
        Thread.sleep(60000L);
        return forTable;
    }

    private static List<String> getAddress(Document doc) {
        Element address = doc.selectFirst("[itemtype=http://schema.org/PostalAddress]");
        if (address != null) {
            String addr = address.selectFirst("span[class=style-item-address__string-wt61A]").text().replaceAll("обл.", "область");
            return new ArrayList<>(Arrays.asList(addr.split(", ")));
        }
        return new ArrayList<>();
    }

    private static List<String> getHeaders(Document doc) {
        Elements headersList = doc.getElementsByAttributeValue("itemtype", "http://schema.org/BreadcrumbList");
        List<String> breadcrumbs = new ArrayList<>();
        if (!headersList.isEmpty()) {
            Element header = headersList.first();
            Elements names = header.getElementsByAttributeValue("itemprop", "name");
            for (Element name : names) {
                breadcrumbs.add(name.text());
            }
        }
        return breadcrumbs;
    }

    private static String getState(Document doc) {
        Elements items = doc.select("li.params-paramsList__item-_2Y2O");
        String state = null;
        for (Element item : items) {
            Element span = item.select("span:contains(Состояние)").first();
            if (span != null) {
                state = item.ownText();
            }
        }
        return state;
    }
}
