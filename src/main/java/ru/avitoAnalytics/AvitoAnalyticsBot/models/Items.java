package ru.avitoAnalytics.AvitoAnalyticsBot.models;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Items {
    private String itemId;
    private List<Stats> stats = new ArrayList<>();

    @Setter
    private String sheetsLink;

    @Setter
    private String range;

    @Setter
    private double cost;

}
