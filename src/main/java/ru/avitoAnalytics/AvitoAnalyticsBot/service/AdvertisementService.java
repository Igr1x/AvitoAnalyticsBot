package ru.avitoAnalytics.AvitoAnalyticsBot.service;

import ru.avitoAnalytics.AvitoAnalyticsBot.entity.AccountData;
import ru.avitoAnalytics.AvitoAnalyticsBot.models.Advertisement;

import java.util.List;

public interface AdvertisementService {

    List<Advertisement> getAdvertisements(AccountData accountData, String perPage, String page, String statusAdvertisement);

}
