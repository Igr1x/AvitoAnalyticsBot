package ru.avitoAnalytics.AvitoAnalyticsBot.models;

import lombok.Getter;

@Getter
public class Stats {
    private String date;
    private int uniqContacts;
    private int uniqFavorites;
    private int uniqViews;

    private double cv = 0.0;
    private double sumViews = 0.0;
    private double sumPod = 0.0;
    private double totalAmount = 0.0;
    private double sumCon = 0;

    public Stats() {
        if (uniqViews != 0) {
            cv = (double) uniqContacts / uniqViews;
        }
    }

    @Override
    public String toString() {
        return "Stats{" +
                "date='" + date + '\'' +
                ", uniqContacts=" + uniqContacts +
                ", uniqFavorites=" + uniqFavorites +
                ", uniqViews=" + uniqViews +
                ", totalAmount=" + totalAmount +
                '}';
    }
}
