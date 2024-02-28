package ru.avitoAnalytics.AvitoAnalyticsBot.service;

import javax.security.auth.login.AccountNotFoundException;
import java.util.List;
import java.util.Map;

public interface CollectingInfoOnAdsService {

    Map<String, List<Long>> getInfoOnAdvertisement(Long userId) throws AccountNotFoundException;

}
