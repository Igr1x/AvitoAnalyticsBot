package ru.avitoAnalytics.AvitoAnalyticsBot.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.FavouriteItems;
import ru.avitoAnalytics.AvitoAnalyticsBot.repositories.FavouriteItemsRepository;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.FavouriteItemsService;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class FavouriteItemsServiceImpl implements FavouriteItemsService {
    FavouriteItemsRepository itemsRepository;

    @Override
    public BigDecimal findCostById(Long id) {
         var item = itemsRepository.findById(id).orElseGet (() -> {
             return itemsRepository.save(new FavouriteItems(id, BigDecimal.valueOf(0.0)));
         });
        return item.getCost();
    }

    @Override
    public FavouriteItems save(FavouriteItems item) {
        return itemsRepository.save(item);
    }
}
