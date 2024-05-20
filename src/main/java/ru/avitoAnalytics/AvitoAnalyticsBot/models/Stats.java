package ru.avitoAnalytics.AvitoAnalyticsBot.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Stats {
    private String dayOfWeek;
    private String date;
    private int uniqContacts;
    private int uniqFavorites;
    private int uniqViews;

    private double cv = 0.0;
    private double sumViews = 0.0;
    private double sumRaise = 0.0;
    private double totalSum = 0.0;
    private double sumContact = 0;
    private double cost;


    public Stats(String date, int uniqContacts, int uniqFavorites, int uniqViews, double sumRaise) {
        this.date = date;
        this.uniqContacts = uniqContacts;
        this.uniqFavorites = uniqFavorites;
        this.uniqViews = uniqViews;
        this.sumRaise = sumRaise;
    }

    public Stats(String dayOfWeek, String date) {
        this.dayOfWeek = dayOfWeek;
        this.date = date;
    }

    public void updateFields(double cost) {
        this.cost = cost;
        sumViews = uniqViews * cost;
        if (uniqViews != 0) {
            cv = (double) uniqContacts / uniqViews;
        }
        totalSum = sumRaise + sumViews;
        if (uniqContacts != 0) {
            sumContact = totalSum / uniqContacts;
        }
    }

    public void updateSumRaise(double sumRaise) {
        this.sumRaise += sumRaise;
    }
}
