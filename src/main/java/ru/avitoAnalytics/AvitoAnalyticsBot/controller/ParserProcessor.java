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
        if (set.add(ad.getAvitoId())) {
            queue.add(ad);
        }
    }

    public void addListAds(List<Ads> adsList) {
        System.out.println("getListAds: " + adsList.size());
        for (Ads ad : adsList) {
            if (set.add(ad.getAvitoId())) {
                queue.add(ad);
            }
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
                }
            } catch (ItemNotFoundException e) {
                log.error(e.getMessage());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }

    private BigDecimal processAd(Long id) throws Exception {
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
        String category;
        String subcategory;
        String lastCategory;
        if (ads.getCategories().size() == 3) {
            category = categories.get(0);
            subcategory = categories.get(1);
            lastCategory = categories.get(2);
        } else {
            throw new ItemNotFoundException(String.format("Item %d has empty categories", id));
        }
        return avitoCostJdbcRepository.findAvitoCost(region, city, street, category, subcategory, lastCategory)
                .map(AvitoCost::getCost)
                .orElse(BigDecimal.ZERO);
    }
}

