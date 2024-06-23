package ru.avitoAnalytics.AvitoAnalyticsBot.controller;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.Ads;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AvitoCost;
import ru.avitoAnalytics.AvitoAnalyticsBot.exceptions.ItemNotFoundException;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.Product;
import ru.avitoAnalytics.AvitoAnalyticsBot.repositories.AdsRepository;
import ru.avitoAnalytics.AvitoAnalyticsBot.repositories.AvitoCostJdbcRepository;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.ParserService;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.impl.AdsServiceImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class ParserProcessor extends Thread {

    private final BlockingQueue<Ads> queue = new LinkedBlockingQueue<>();
    private final Set<Long> set = ConcurrentHashMap.newKeySet();

    private final ParserService parserService;
    private final AdsRepository adsRepository;
    private final AvitoCostJdbcRepository avitoCostJdbcRepository;
    private final AdsServiceImpl adsService;

    @PostConstruct
    public void init() {
        this.start();
    }

    public void addAds(Ads ad) {
        System.out.println("Try to add in queue: " + ad.getAvitoId());
        if (set.add(ad.getAvitoId())) {
            queue.add(ad);
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                Ads ad = queue.poll(1, TimeUnit.MINUTES);
                if (ad != null) {
                    set.remove(ad.getAvitoId());
                    var cost = processAd(ad.getAvitoId());
                    ad.setCost(cost);
                    adsService.save(ad);
                    System.out.printf("объявление спаршено! %d\n", ad.getAvitoId());
                } else {
                    System.out.println("Очередь пуста");
                }
            } catch (ItemNotFoundException e) {
                log.error(e.getMessage());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private BigDecimal processAd(Long id) {
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
        return avitoCostJdbcRepository.findAvitoCost(region, city, street, category, subcategory, lastCategory)
                .map(AvitoCost::getCost)
                .orElse(BigDecimal.ZERO);
    }

    private String categoryProcess(String category) {
        String[] parts = category.split(" ");
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

