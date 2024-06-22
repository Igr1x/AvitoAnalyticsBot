package ru.avitoAnalytics.AvitoAnalyticsBot.controller;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.Ads;
import ru.avitoAnalytics.AvitoAnalyticsBot.exceptions.ItemNotFoundException;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.Product;
import ru.avitoAnalytics.AvitoAnalyticsBot.repositories.AdsRepository;
import ru.avitoAnalytics.AvitoAnalyticsBot.repositories.AvitoCostJdbcRepository;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.ParserService;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.impl.AdsServiceImpl;

import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class ParserProcessor extends Thread {

    private final BlockingQueue<Long> queue = new LinkedBlockingQueue<>();
    private final Set<Long> set = ConcurrentHashMap.newKeySet();

    private final ParserService parserService;
    private final AdsRepository adsRepository;
    private final AvitoCostJdbcRepository avitoCostJdbcRepository;
    private final AdsServiceImpl adsServiceImpl;

    @PostConstruct
    public void init() {
        this.start();
    }

    public void addAds(Long id) {
        offer(id);
    }

    private boolean offer(Long value) {
        System.out.println("Try to add in queue: " + value);
        return set.add(value) & queue.offer(value);
    }

    @Override
    public void run() {
        while (true) {
            try {
                Long id = queue.poll(1, TimeUnit.MINUTES);
                if (id != null) {
                    set.remove(id);
                    processAd(id);
                    System.out.printf("объявление спаршено! %d\n", id);
                } else {
                    System.out.println("Очередь пуста");
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void processAd(Long id) {
        Product ads = parserService.parseProduct(id);
        List<String> categories = ads.getCategories();
        if (categories == null || categories.isEmpty()) {
            throw new ItemNotFoundException(String.format("Item %d has empty categories", id));
        }
        String address = ads.getAddress();
        List<String> addressParts = List.of(address.split(", "));
        String region = addressParts.get(0);
        String city = "";
        String street = "";
        if (addressParts.size() >= 2) {
            city = addressParts.get(1);
        }
        if (addressParts.size() >= 3) {
            street = addressParts.get(2);
        }
        String category = categoryProcess(categories.get(1));
        String subcategory = categoryProcess(categories.get(2));
        String lastCategory = categories.get(categories.size() - 1);
        var item = avitoCostJdbcRepository.findAvitoCost(region, city, street, category, subcategory, lastCategory);

        System.out.println(ads.toString());
    }

    private String categoryProcess(String address) {
        String[] parts = address.split(" ");
        StringBuilder sb = new StringBuilder();
        int size = parts.length;
        if (size <= 2) {
            throw new ItemNotFoundException(String.format("Item has incorrect address - %s", parts));
        }
        for (int i = 0; i < size - 2; i++) {
            sb.append(parts[i]);
            if (i != size - 3) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }
}

