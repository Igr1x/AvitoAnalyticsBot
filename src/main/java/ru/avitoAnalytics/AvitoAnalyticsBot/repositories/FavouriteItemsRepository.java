package ru.avitoAnalytics.AvitoAnalyticsBot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.FavouriteItems;

import java.util.Optional;

@Repository
public interface FavouriteItemsRepository extends JpaRepository<FavouriteItems, Long> {

}
