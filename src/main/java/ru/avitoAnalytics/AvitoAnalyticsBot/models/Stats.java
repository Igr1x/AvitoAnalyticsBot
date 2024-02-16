package ru.avitoAnalytics.AvitoAnalyticsBot.models;

import lombok.Getter;

@Getter
public class Stats {
    private String date;
    private int uniqContacts;
    private int uniqFavorites;
    private int uniqViews;
    private double totalAmount = 0;

    public void setTotalAmount(double totalAmount) {
        this.totalAmount += totalAmount;
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
