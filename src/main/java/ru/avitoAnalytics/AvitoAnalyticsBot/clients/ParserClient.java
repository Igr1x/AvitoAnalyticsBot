package ru.avitoAnalytics.AvitoAnalyticsBot.clients;

import ru.avitoAnalytics.AvitoAnalyticsBot.models.Product;

public interface ParserClient {

    Product  parseAdvertisement(long id);

}
