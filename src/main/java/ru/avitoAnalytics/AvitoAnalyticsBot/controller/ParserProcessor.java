package ru.avitoAnalytics.AvitoAnalyticsBot.controller;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.Ads;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AvitoCost;
import ru.avitoAnalytics.AvitoAnalyticsBot.exceptions.ItemNotFoundException;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.Product;
import ru.avitoAnalytics.AvitoAnalyticsBot.repositories.AvitoCostJdbcRepository;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.AdsService;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.CityService;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.ParserService;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    private final AvitoCostJdbcRepository avitoCostJdbcRepository;
    private final AdsService adsService;
    private final CityService cityService;

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
            Long adId = 0L;
            try {
                Ads ad = queue.poll(1, TimeUnit.MINUTES);
                adId = ad.getAvitoId();
                if (ad != null) {
                    set.remove(adId);
                    var newAd = processAd(ad);
                    adsService.save(newAd);
                }
            } catch (ItemNotFoundException e) {
                set.remove(adId);
                log.error(e.getMessage());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                set.remove(adId);
                log.error(e.getMessage());
            }
        }
    }

    private Ads processAd(Ads ad) throws Exception {
        Product ads = parserService.parseProduct(ad.getAvitoId());
        List<String> categories = ads.getCategories();
        if (categories == null || categories.isEmpty()) {
            throw new ItemNotFoundException(String.format("Item %d has empty categories", ad));
        }
        String address = addressProcessor(ads.getAddress());
        String category;
        String subcategory;
        String lastCategory;
        if (ads.getCategories().size() == 3) {
            category = categories.get(0);
            subcategory = categories.get(1);
            lastCategory = categories.get(2);
        } else {
            throw new ItemNotFoundException(String.format("Item %d has empty categories", ad));
        }
        var cost = avitoCostJdbcRepository.findAvitoCost(address, address, address, category, subcategory, lastCategory)
                .map(AvitoCost::getCost)
                .orElse(BigDecimal.ZERO);
        ad.setCost(cost);
        ad.setPubDate(LocalDate.now().minusDays(1));
        ad.setCity(address);
        return ad;
    }

    private String addressProcessor(String address) {
        List<String> addressParts = List.of(address.split(", "));
        var city = cityService.getCityByName(addressParts.get(0));
        if (city.isPresent()) {
            return city.get().getCityName();
        }
        if (addressParts.size() >= 2) {
            city = cityService.getCityByName(addressParts.get(1));
            if (city.isPresent()) {
                return city.get().getCityName();
            }
        }
        if (addressParts.size() >= 3) {
            city = cityService.getCityByName(addressParts.get(2));
            if (city.isPresent()) {
                return city.get().getCityName();
            }
        }
        return null;
    }
}

