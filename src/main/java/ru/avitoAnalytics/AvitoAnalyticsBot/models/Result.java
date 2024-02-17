package ru.avitoAnalytics.AvitoAnalyticsBot.models;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Result {
    List<Items> items = new ArrayList<>();
}
