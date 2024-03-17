package ru.avitoAnalytics.AvitoAnalyticsBot.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class StatSummary {
    private String dayOfWeek;
    private String date;
    private Object uniqViews = 0;
    private Object cv = 0;
    private Object uniqContacts = 0;
    private Object uniqFavorites = 0;
    private Object sumViews = 0;
    private Object sumRaise = 0;
    private Object totalSum = 0;
    private Object sumContact = 0;

    public StatSummary(String dayOfWeek, String date) {
        this.dayOfWeek = dayOfWeek;
        this.date = date;
    }
}
