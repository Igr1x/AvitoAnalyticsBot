package ru.avitoAnalytics.AvitoAnalyticsBot.models;

import lombok.Getter;
import lombok.Setter;

@Getter
public class Stats {
    private String date;
    private int uniqContacts;
    private int uniqFavorites;
    private int uniqViews;

    private double cv = 0.0;
    private double sumViews = 0.0;
    private double sumRaise = 0.0;
    private double totalSum = 0.0;
    private double sumContact = 0;

    @Setter
    private double cost;

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

    /*public void updateSum() {
        totalSum = sumRaise + sumViews;
        if (uniqContacts != 0) {
            sumContact = totalSum / uniqContacts;
        }
    }*/

    public void updateSumRaise(double sumRaise) {
        this.sumRaise += sumRaise;
    }
}
