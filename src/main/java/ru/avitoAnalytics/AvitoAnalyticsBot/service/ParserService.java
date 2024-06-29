package ru.avitoAnalytics.AvitoAnalyticsBot.service;

import ru.avitoAnalytics.AvitoAnalyticsBot.models.Product;

import java.util.List;

public interface ParserService {

    Product parseProduct(long id) throws Exception;

    List<Product> parseProducts(List<Long> ids);
}
