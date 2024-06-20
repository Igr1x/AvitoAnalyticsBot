package ru.avitoAnalytics.AvitoAnalyticsBot.service;

import ru.avitoAnalytics.AvitoAnalyticsBot.models.Advertisement;

import java.util.List;

public interface AdvertisementAvitoService {

    List<Advertisement> getAllAdvertisements(String token, String updatedAtFrom);

}
